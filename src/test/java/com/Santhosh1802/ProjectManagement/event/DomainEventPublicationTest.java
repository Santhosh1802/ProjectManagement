package com.Santhosh1802.ProjectManagement.event;

import com.Santhosh1802.ProjectManagement.dto.request.task.CreateTaskRequest;
import com.Santhosh1802.ProjectManagement.dto.request.task.UpdateTaskStatusRequest;
import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.Task;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.repository.ProjectMemberRepository;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.repository.TaskRepository;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.service.TaskService;
import com.Santhosh1802.ProjectManagement.tasks.event.TaskCreatedEvent;
import com.Santhosh1802.ProjectManagement.tasks.event.TaskStatusChangedEvent;
import com.Santhosh1802.ProjectManagement.util.ProjectStatus;
import com.Santhosh1802.ProjectManagement.util.TaskPriority;
import com.Santhosh1802.ProjectManagement.util.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DomainEventPublicationTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private Authentication authentication;

    private TaskService taskService;

    private UUID projectId;
    private UUID assignedUserId;
    private UUID creatorId;
    private UUID taskId;
    private Project project;
    private User assignedUser;
    private User creatorUser;
    private Task task;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(
                taskRepository,
                projectRepository,
                projectMemberRepository,
                userRepository,
                eventPublisher
        );

        projectId = UUID.randomUUID();
        assignedUserId = UUID.randomUUID();
        creatorId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        creatorUser = new User();
        creatorUser.setId(creatorId);
        creatorUser.setEmail("creator@example.com");
        creatorUser.setIsActive(true);

        assignedUser = new User();
        assignedUser.setId(assignedUserId);
        assignedUser.setEmail("dev@example.com");
        assignedUser.setIsActive(true);

        project = new Project();
        project.setId(projectId);
        project.setName("Core Architecture");
        project.setProjectStatus(ProjectStatus.ACTIVE);
        project.setOwner(creatorUser);
        project.setStartDate(Instant.now().minusSeconds(3600));
        project.setDueDate(Instant.now().plusSeconds(86400));

        task = new Task();
        task.setId(taskId);
        task.setTitle("Design Event Schema");
        task.setTaskStatus(TaskStatus.TODO);
        task.setTaskPriority(TaskPriority.HIGH);
        task.setProject(project);
        task.setAssignedTo(assignedUser);
        task.setCreatedBy(creatorUser);
    }

    @Test
    @DisplayName("Should publish TaskCreatedEvent on successful task creation")
    void testTaskCreatedEventPublication() {
        CreateTaskRequest request = new CreateTaskRequest(
                "Design Event Schema",
                "Draft event records",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                Instant.now().plusSeconds(7200),
                projectId,
                assignedUserId
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(assignedUserId)).thenReturn(Optional.of(assignedUser));
        when(authentication.getName()).thenReturn("creator@example.com");
        when(userRepository.findByEmail("creator@example.com")).thenReturn(Optional.of(creatorUser));
        when(projectMemberRepository.existsByProjectIdAndUserId(projectId, assignedUserId)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setId(taskId);
            return t;
        });

        taskService.createTask(request, authentication);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        Object published = eventCaptor.getValue();
        assertInstanceOf(TaskCreatedEvent.class, published);
        TaskCreatedEvent event = (TaskCreatedEvent) published;
        assertEquals(taskId, event.taskId());
        assertEquals(projectId, event.projectId());
        assertEquals(creatorId, event.actorId());
        assertEquals(assignedUserId, event.assignedToId());
        assertEquals(TaskStatus.TODO, event.status());
        assertNotNull(event.eventId());
        assertNotNull(event.occurredAt());
    }

    @Test
    @DisplayName("Should publish TaskStatusChangedEvent on status update")
    void testTaskStatusChangedEventPublication() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(taskId, TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        taskService.updateTaskStatus(request);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        Object published = eventCaptor.getValue();
        assertInstanceOf(TaskStatusChangedEvent.class, published);
        TaskStatusChangedEvent event = (TaskStatusChangedEvent) published;
        assertEquals(taskId, event.taskId());
        assertEquals(TaskStatus.TODO, event.previousStatus());
        assertEquals(TaskStatus.IN_PROGRESS, event.newStatus());
    }

    @Test
    @DisplayName("Should not publish events if operation fails validation")
    void testNoEventOnFailedOperation() {
        CreateTaskRequest request = new CreateTaskRequest(
                "Invalid Task",
                "No project",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                Instant.now().plusSeconds(7200),
                projectId,
                assignedUserId
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskService.createTask(request, authentication));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should not publish events if repository save throws exception")
    void testNoEventWhenPersistenceFails() {
        CreateTaskRequest request = new CreateTaskRequest(
                "Task",
                "Description",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                Instant.now().plusSeconds(7200),
                projectId,
                assignedUserId
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(assignedUserId)).thenReturn(Optional.of(assignedUser));
        when(authentication.getName()).thenReturn("creator@example.com");
        when(userRepository.findByEmail("creator@example.com")).thenReturn(Optional.of(creatorUser));
        when(projectMemberRepository.existsByProjectIdAndUserId(projectId, assignedUserId)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenThrow(new org.springframework.dao.DataIntegrityViolationException("DB error"));

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> taskService.createTask(request, authentication));
        verify(eventPublisher, never()).publishEvent(any());
    }
}
