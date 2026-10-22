package com.Santhosh1802.ProjectManagement.audit;

import com.Santhosh1802.ProjectManagement.audit.dto.response.ActivityLogResponse;
import com.Santhosh1802.ProjectManagement.audit.entity.ActivityLog;
import com.Santhosh1802.ProjectManagement.audit.repository.ActivityLogRepository;
import com.Santhosh1802.ProjectManagement.audit.service.ActivityLogService;
import com.Santhosh1802.ProjectManagement.comment.event.TaskCommentCreatedEvent;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.tasks.event.TaskCreatedEvent;
import com.Santhosh1802.ProjectManagement.tasks.event.TaskStatusChangedEvent;
import com.Santhosh1802.ProjectManagement.util.TaskPriority;
import com.Santhosh1802.ProjectManagement.util.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityLogServiceTest {

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private UserRepository userRepository;

    private ActivityLogService activityLogService;

    private User sampleUser;
    private UUID sampleUserId;
    private UUID sampleTaskId;
    private UUID sampleProjectId;

    @BeforeEach
    void setUp() {
        activityLogService = new ActivityLogService(activityLogRepository, userRepository);

        sampleUserId = UUID.randomUUID();
        sampleTaskId = UUID.randomUUID();
        sampleProjectId = UUID.randomUUID();

        sampleUser = new User();
        sampleUser.setId(sampleUserId);
        sampleUser.setFirstName("Jane");
        sampleUser.setLastName("Doe");
        sampleUser.setEmail("jane@example.com");
    }

    @Test
    @DisplayName("Should record activity when TaskStatusChangedEvent is received")
    void shouldRecordActivityOnTaskStatusChanged() {
        when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(sampleUser));

        TaskStatusChangedEvent event = new TaskStatusChangedEvent(
                UUID.randomUUID(),
                sampleTaskId,
                sampleProjectId,
                sampleUserId,
                TaskStatus.TODO,
                TaskStatus.IN_PROGRESS,
                Instant.now()
        );

        activityLogService.onTaskStatusChanged(event);

        ArgumentCaptor<ActivityLog> captor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(activityLogRepository).save(captor.capture());

        ActivityLog saved = captor.getValue();
        assertThat(saved.getTaskId()).isEqualTo(sampleTaskId);
        assertThat(saved.getProjectId()).isEqualTo(sampleProjectId);
        assertThat(saved.getActorId()).isEqualTo(sampleUserId);
        assertThat(saved.getActorName()).isEqualTo("Jane Doe");
        assertThat(saved.getActorEmail()).isEqualTo("jane@example.com");
        assertThat(saved.getActionType()).isEqualTo("TASK_STATUS_CHANGED");
        assertThat(saved.getDetails()).contains("TODO to IN_PROGRESS");
    }

    @Test
    @DisplayName("Should record activity when TaskCreatedEvent is received")
    void shouldRecordActivityOnTaskCreated() {
        when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(sampleUser));

        TaskCreatedEvent event = new TaskCreatedEvent(
                UUID.randomUUID(),
                sampleTaskId,
                sampleProjectId,
                sampleUserId,
                null,
                "New Feature Task",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                Instant.now()
        );

        activityLogService.onTaskCreated(event);

        ArgumentCaptor<ActivityLog> captor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(activityLogRepository).save(captor.capture());

        ActivityLog saved = captor.getValue();
        assertThat(saved.getTaskId()).isEqualTo(sampleTaskId);
        assertThat(saved.getActionType()).isEqualTo("TASK_CREATED");
        assertThat(saved.getDetails()).contains("New Feature Task");
    }

    @Test
    @DisplayName("Should record activity when TaskCommentCreatedEvent is received")
    void shouldRecordActivityOnTaskCommentCreated() {
        when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(sampleUser));

        TaskCommentCreatedEvent event = new TaskCommentCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                sampleTaskId,
                sampleProjectId,
                sampleUserId,
                Instant.now()
        );

        activityLogService.onTaskCommentCreated(event);

        ArgumentCaptor<ActivityLog> captor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(activityLogRepository).save(captor.capture());

        ActivityLog saved = captor.getValue();
        assertThat(saved.getTaskId()).isEqualTo(sampleTaskId);
        assertThat(saved.getActionType()).isEqualTo("TASK_COMMENT_CREATED");
        assertThat(saved.getDetails()).isEqualTo("Posted a comment");
    }

    @Test
    @DisplayName("Should retrieve task activity logs in order")
    void shouldRetrieveTaskActivityLogs() {
        ActivityLog log = ActivityLog.builder()
                .id(UUID.randomUUID())
                .taskId(sampleTaskId)
                .projectId(sampleProjectId)
                .actorId(sampleUserId)
                .actorName("Jane Doe")
                .actorEmail("jane@example.com")
                .actionType("TASK_STATUS_CHANGED")
                .details("Status changed")
                .createdAt(Instant.now())
                .build();

        when(activityLogRepository.findByTaskIdOrderByCreatedAtDesc(sampleTaskId))
                .thenReturn(List.of(log));

        List<ActivityLogResponse> results = activityLogService.getTaskActivity(sampleTaskId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).actionType()).isEqualTo("TASK_STATUS_CHANGED");
        assertThat(results.get(0).actorName()).isEqualTo("Jane Doe");
    }
}
