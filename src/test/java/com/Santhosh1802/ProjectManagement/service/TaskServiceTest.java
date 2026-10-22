package com.Santhosh1802.ProjectManagement.service;

import com.Santhosh1802.ProjectManagement.dto.request.task.CreateTaskRequest;
import com.Santhosh1802.ProjectManagement.dto.request.task.GetTaskByIdRequest;
import com.Santhosh1802.ProjectManagement.dto.request.task.UpdateTaskDetailsRequest;
import com.Santhosh1802.ProjectManagement.dto.request.task.UpdateTaskStatusRequest;
import com.Santhosh1802.ProjectManagement.dto.response.task.GetTaskResponse;
import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.Task;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.exception.project.ProjectNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.task.TaskNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException;
import com.Santhosh1802.ProjectManagement.repository.ProjectMemberRepository;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.repository.TaskRepository;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.util.ProjectStatus;
import com.Santhosh1802.ProjectManagement.util.TaskPriority;
import com.Santhosh1802.ProjectManagement.util.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private UUID taskId;
    private UUID projectId;
    private UUID assignedUserId;
    private UUID createdByUserId;

    private User owner;
    private User assignedUser;
    private User createdBy;

    private Project project;
    private Task task;

    private Instant dueDate;
    private Instant createdAt;
    private Instant updatedAt;

    @BeforeEach
    void setUp() {

        taskId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        assignedUserId = UUID.randomUUID();
        createdByUserId = UUID.randomUUID();

        dueDate = Instant.parse("2026-12-31T10:00:00Z");
        createdAt = Instant.parse("2026-10-01T10:00:00Z");
        updatedAt = Instant.parse("2026-10-02T10:00:00Z");

        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setFirstName("Project");
        owner.setLastName("Owner");
        owner.setEmail("owner@example.com");

        assignedUser = new User();
        assignedUser.setId(assignedUserId);
        assignedUser.setFirstName("Assigned");
        assignedUser.setLastName("User");
        assignedUser.setEmail("assigned@example.com");

        createdBy = new User();
        createdBy.setId(createdByUserId);
        createdBy.setFirstName("Created");
        createdBy.setLastName("User");
        createdBy.setEmail("creator@example.com");

        project = new Project();
        project.setId(projectId);
        project.setName("DevFlow");
        project.setDescription("Project management application");
        project.setProjectStatus(ProjectStatus.ACTIVE);
        project.setOwner(owner);
        project.setCreatedAt(createdAt);
        project.setUpdatedAt(updatedAt);

        task = new Task();
        task.setId(taskId);
        task.setTitle("Implement authentication");
        task.setDescription("Implement session based authentication");
        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        task.setTaskPriority(TaskPriority.HIGH);
        task.setDueDate(dueDate);
        task.setProject(project);
        task.setAssignedTo(assignedUser);
        task.setCreatedBy(createdBy);
        task.setCreatedAt(createdAt);
        task.setUpdatedAt(updatedAt);
    }

    @Test
    void createTask_shouldCreateAndReturnTask() {

        CreateTaskRequest request = new CreateTaskRequest(
                "Implement authentication",
                "Implement session based authentication",
                TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH,
                dueDate,
                projectId,
                assignedUserId
        );

        org.springframework.security.core.Authentication authentication = mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("creator@example.com");

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(userRepository.findById(assignedUserId))
                .thenReturn(Optional.of(assignedUser));

        when(userRepository.findByEmail("creator@example.com"))
                .thenReturn(Optional.of(createdBy));

        when(projectMemberRepository.existsByProjectIdAndUserId(projectId, assignedUserId))
                .thenReturn(true);

        when(taskRepository.save(any(Task.class)))
                .thenReturn(task);

        GetTaskResponse response = taskService.createTask(request, authentication);

        assertNotNull(response);
        assertEquals(taskId, response.getId());
        assertEquals("Implement authentication", response.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, response.getTaskStatus());
        assertEquals(TaskPriority.HIGH, response.getTaskPriority());

        verify(projectRepository).findById(projectId);
        verify(userRepository).findById(assignedUserId);
        verify(userRepository).findByEmail("creator@example.com");
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_shouldThrowWhenProjectNotFound() {

        CreateTaskRequest request = new CreateTaskRequest(
                "Implement authentication",
                "Implement session based authentication",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                dueDate,
                projectId,
                assignedUserId
        );

        org.springframework.security.core.Authentication authentication = mock(org.springframework.security.core.Authentication.class);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectNotFoundException.class,
                () -> taskService.createTask(request, authentication)
        );

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_shouldThrowWhenAssignedUserNotFound() {

        CreateTaskRequest request = new CreateTaskRequest(
                "Implement authentication",
                "Implement session based authentication",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                dueDate,
                projectId,
                assignedUserId
        );

        org.springframework.security.core.Authentication authentication = mock(org.springframework.security.core.Authentication.class);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(userRepository.findById(assignedUserId))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> taskService.createTask(request, authentication)
        );

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_shouldThrowWhenCreatedByUserNotFound() {

        CreateTaskRequest request = new CreateTaskRequest(
                "Implement authentication",
                "Implement session based authentication",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                dueDate,
                projectId,
                assignedUserId
        );

        org.springframework.security.core.Authentication authentication = mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("unknown@example.com");

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(userRepository.findById(assignedUserId))
                .thenReturn(Optional.of(assignedUser));

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> taskService.createTask(request, authentication)
        );

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void getTaskById_shouldReturnTask() {

        GetTaskByIdRequest request =
                new GetTaskByIdRequest(taskId);

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        GetTaskResponse response =
                taskService.getTaskById(request);

        assertNotNull(response);
        assertEquals(taskId, response.getId());
        assertEquals(task.getTitle(), response.getTitle());
        assertEquals(task.getDescription(), response.getDescription());
        assertEquals(task.getTaskStatus(), response.getTaskStatus());
        assertEquals(task.getTaskPriority(), response.getTaskPriority());
        assertEquals(task.getDueDate(), response.getDueDate());

        assertNotNull(response.getProject());
        assertNotNull(response.getAssignedTo());
        assertNotNull(response.getCreatedBy());

        verify(taskRepository).findById(taskId);
    }

    @Test
    void getTaskById_shouldThrowWhenTaskNotFound() {

        GetTaskByIdRequest request =
                new GetTaskByIdRequest(taskId);

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class,
                () -> taskService.getTaskById(request)
        );
    }

    @Test
    void getAllTasks_shouldReturnPaginatedTasks() {

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Task> page =
                new PageImpl<>(
                        List.of(task),
                        pageable,
                        1
                );

        when(taskRepository.findAll(pageable))
                .thenReturn(page);

        var response =
                taskService.getAllTasks(0, 10);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());

        verify(taskRepository).findAll(pageable);
    }

    @Test
    void getTasksByProject_shouldReturnPaginatedTasks() {

        GetTaskByIdRequest request =
                new GetTaskByIdRequest(projectId);

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Task> page =
                new PageImpl<>(
                        List.of(task),
                        pageable,
                        1
                );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(taskRepository.findByProject(project, pageable))
                .thenReturn(page);

        var response =
                taskService.getTasksByProject(
                        request,
                        0,
                        10
                );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(taskId, response.getContent().getFirst().getId());

        verify(projectRepository).findById(projectId);
        verify(taskRepository).findByProject(project, pageable);
    }

    @Test
    void getTasksByProject_shouldThrowWhenProjectNotFound() {

        GetTaskByIdRequest request =
                new GetTaskByIdRequest(projectId);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectNotFoundException.class,
                () -> taskService.getTasksByProject(
                        request,
                        0,
                        10
                )
        );

        verify(taskRepository, never())
                .findByProject(any(), any());
    }

    @Test
    void getTasksAssignedToUser_shouldReturnPaginatedTasks() {

        GetTaskByIdRequest request =
                new GetTaskByIdRequest(assignedUserId);

        PageRequest pageable = PageRequest.of(1, 5);

        Page<Task> page =
                new PageImpl<>(
                        List.of(task),
                        pageable,
                        6
                );

        when(userRepository.findById(assignedUserId))
                .thenReturn(Optional.of(assignedUser));

        when(taskRepository.findByAssignedTo(
                assignedUser,
                pageable
        )).thenReturn(page);

        var response =
                taskService.getTasksAssignedToUser(
                        request,
                        1,
                        5
                );

        assertNotNull(response);
        assertEquals(1, response.getPage());
        assertEquals(5, response.getSize());
        assertEquals(6, response.getTotalElements());
        assertEquals(2, response.getTotalPages());

        verify(userRepository).findById(assignedUserId);
        verify(taskRepository).findByAssignedTo(
                assignedUser,
                pageable
        );
    }

    @Test
    void getTasksAssignedToUser_shouldThrowWhenUserNotFound() {

        GetTaskByIdRequest request =
                new GetTaskByIdRequest(assignedUserId);

        when(userRepository.findById(assignedUserId))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> taskService.getTasksAssignedToUser(
                        request,
                        0,
                        10
                )
        );

        verify(taskRepository, never())
                .findByAssignedTo(any(), any());
    }

    @Test
    void getTasksCreatedByUser_shouldReturnPaginatedTasks() {

        GetTaskByIdRequest request =
                new GetTaskByIdRequest(createdByUserId);

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Task> page =
                new PageImpl<>(
                        List.of(task),
                        pageable,
                        1
                );

        when(userRepository.findById(createdByUserId))
                .thenReturn(Optional.of(createdBy));

        when(taskRepository.findByCreatedBy(
                createdBy,
                pageable
        )).thenReturn(page);

        var response =
                taskService.getTasksCreatedByUser(
                        request,
                        0,
                        10
                );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(taskId, response.getContent().getFirst().getId());

        verify(userRepository).findById(createdByUserId);
        verify(taskRepository).findByCreatedBy(
                createdBy,
                pageable
        );
    }

    @Test
    void getTasksCreatedByUser_shouldThrowWhenUserNotFound() {

        GetTaskByIdRequest request =
                new GetTaskByIdRequest(createdByUserId);

        when(userRepository.findById(createdByUserId))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> taskService.getTasksCreatedByUser(
                        request,
                        0,
                        10
                )
        );

        verify(taskRepository, never())
                .findByCreatedBy(any(), any());
    }

    @Test
    void updateTaskDetails_shouldUpdateAndReturnTask() {

        UpdateTaskDetailsRequest request =
                new UpdateTaskDetailsRequest(
                        taskId,
                        "Updated title",
                        "Updated task description",
                        TaskStatus.DONE,
                        TaskPriority.URGENT,
                        dueDate,
                        projectId,
                        assignedUserId,
                        createdByUserId
                );

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(userRepository.findById(assignedUserId))
                .thenReturn(Optional.of(assignedUser));

        when(userRepository.findById(createdByUserId))
                .thenReturn(Optional.of(createdBy));

        when(projectMemberRepository.existsByProjectIdAndUserId(projectId, assignedUserId))
                .thenReturn(true);

        when(taskRepository.save(task))
                .thenReturn(task);

        GetTaskResponse response =
                taskService.updateTaskDetails(request);

        assertNotNull(response);

        assertEquals("Updated title", task.getTitle());
        assertEquals(
                "Updated task description",
                task.getDescription()
        );
        assertEquals(TaskStatus.DONE, task.getTaskStatus());
        assertEquals(TaskPriority.URGENT, task.getTaskPriority());
        assertEquals(project, task.getProject());
        assertEquals(assignedUser, task.getAssignedTo());
        assertEquals(createdBy, task.getCreatedBy());

        verify(taskRepository).findById(taskId);
        verify(projectRepository).findById(projectId);
        verify(userRepository).findById(assignedUserId);
        verify(userRepository).findById(createdByUserId);
        verify(taskRepository).save(task);
    }

    @Test
    void updateTaskDetails_shouldThrowWhenTaskNotFound() {

        UpdateTaskDetailsRequest request =
                new UpdateTaskDetailsRequest(
                        taskId,
                        "Updated title",
                        "Updated task description",
                        TaskStatus.DONE,
                        TaskPriority.HIGH,
                        dueDate,
                        projectId,
                        assignedUserId,
                        createdByUserId
                );

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class,
                () -> taskService.updateTaskDetails(request)
        );

        verify(projectRepository, never()).findById(any());
        verify(userRepository, never()).findById(any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTaskDetails_shouldThrowWhenProjectNotFound() {

        UpdateTaskDetailsRequest request =
                new UpdateTaskDetailsRequest(
                        taskId,
                        "Updated title",
                        "Updated task description",
                        TaskStatus.DONE,
                        TaskPriority.HIGH,
                        dueDate,
                        projectId,
                        assignedUserId,
                        createdByUserId
                );

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectNotFoundException.class,
                () -> taskService.updateTaskDetails(request)
        );

        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTaskDetails_shouldThrowWhenAssignedUserNotFound() {

        UpdateTaskDetailsRequest request =
                new UpdateTaskDetailsRequest(
                        taskId,
                        "Updated title",
                        "Updated task description",
                        TaskStatus.DONE,
                        TaskPriority.HIGH,
                        dueDate,
                        projectId,
                        assignedUserId,
                        createdByUserId
                );

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(userRepository.findById(assignedUserId))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> taskService.updateTaskDetails(request)
        );

        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTaskDetails_shouldThrowWhenCreatedByUserNotFound() {

        UpdateTaskDetailsRequest request =
                new UpdateTaskDetailsRequest(
                        taskId,
                        "Updated title",
                        "Updated task description",
                        TaskStatus.DONE,
                        TaskPriority.HIGH,
                        dueDate,
                        projectId,
                        assignedUserId,
                        createdByUserId
                );

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(userRepository.findById(assignedUserId))
                .thenReturn(Optional.of(assignedUser));

        when(userRepository.findById(createdByUserId))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> taskService.updateTaskDetails(request)
        );

        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTaskStatus_shouldUpdateStatus() {

        UpdateTaskStatusRequest request =
                new UpdateTaskStatusRequest(
                        taskId,
                        TaskStatus.DONE
                );

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        when(taskRepository.save(task))
                .thenReturn(task);

        GetTaskResponse response =
                taskService.updateTaskStatus(request);

        assertNotNull(response);
        assertEquals(TaskStatus.DONE, task.getTaskStatus());

        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(task);
    }

    @Test
    void updateTaskStatus_shouldThrowWhenTaskNotFound() {

        UpdateTaskStatusRequest request =
                new UpdateTaskStatusRequest(
                        taskId,
                        TaskStatus.DONE
                );

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class,
                () -> taskService.updateTaskStatus(request)
        );

        verify(taskRepository, never()).save(any());
    }

    @Test
    void deleteTask_shouldDeleteAndReturnTrue() {

        GetTaskByIdRequest request =
                new GetTaskByIdRequest(taskId);

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        boolean result =
                taskService.deleteTask(request);

        assertTrue(result);

        verify(taskRepository).findById(taskId);
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_shouldThrowWhenTaskNotFound() {

        GetTaskByIdRequest request =
                new GetTaskByIdRequest(taskId);

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class,
                () -> taskService.deleteTask(request)
        );

        verify(taskRepository, never()).delete(any());
    }
}
