package com.Santhosh1802.ProjectManagement.notification;

import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.Task;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.notification.dto.response.NotificationResponse;
import com.Santhosh1802.ProjectManagement.notification.entity.Notification;
import com.Santhosh1802.ProjectManagement.notification.entity.NotificationType;
import com.Santhosh1802.ProjectManagement.notification.repository.NotificationRepository;
import com.Santhosh1802.ProjectManagement.notification.service.NotificationService;
import com.Santhosh1802.ProjectManagement.realtime.service.RealtimePushService;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.repository.TaskRepository;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private RealtimePushService realtimePushService;

    private NotificationService notificationService;

    private User recipient;
    private User creator;
    private UUID recipientId;
    private UUID creatorId;
    private Authentication recipientAuth;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationRepository,
                userRepository,
                taskRepository,
                projectRepository,
                realtimePushService
        );

        recipientId = UUID.randomUUID();
        recipient = new User();
        recipient.setId(recipientId);
        recipient.setEmail("recipient@example.com");
        recipient.setFirstName("Alice");

        creatorId = UUID.randomUUID();
        creator = new User();
        creator.setId(creatorId);
        creator.setEmail("creator@example.com");
        creator.setFirstName("Bob");

        recipientAuth = new UsernamePasswordAuthenticationToken(
                recipient.getEmail(),
                "password",
                java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
        );
    }


    @Test
    @DisplayName("Should generate and push notification when task is assigned to another user")
    void shouldGenerateNotificationOnTaskCreated() {
        when(userRepository.findById(recipientId)).thenReturn(Optional.of(recipient));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId(UUID.randomUUID());
            return n;
        });

        TaskCreatedEvent event = new TaskCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                creatorId,
                recipientId,
                "New Feature Task",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                java.time.Instant.now()
        );

        notificationService.onTaskCreated(event);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification saved = notificationCaptor.getValue();

        assertThat(saved.getRecipient()).isEqualTo(recipient);
        assertThat(saved.getType()).isEqualTo(NotificationType.TASK_ASSIGNED);
        assertThat(saved.getTitle()).isEqualTo("New Task Assigned");
        assertThat(saved.getMessage()).contains("New Feature Task");

        verify(realtimePushService).sendNotificationToUser(eq(recipient.getEmail()), any(NotificationResponse.class));
    }

    @Test
    @DisplayName("Should not notify if task creator assigns task to themselves")
    void shouldNotNotifyOnSelfAssignment() {
        TaskCreatedEvent event = new TaskCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                creatorId,
                creatorId, // self-assigned
                "My Own Task",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                java.time.Instant.now()
        );

        notificationService.onTaskCreated(event);

        verifyNoInteractions(notificationRepository);
        verifyNoInteractions(realtimePushService);
    }

    @Test
    @DisplayName("Should notify assignee when task status changes")
    void shouldNotifyAssigneeOnStatusChange() {
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setTitle("Review Pull Request");
        task.setAssignedTo(recipient);
        task.setCreatedBy(creator);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(UUID.randomUUID());
            return n;
        });

        TaskStatusChangedEvent event = new TaskStatusChangedEvent(
                UUID.randomUUID(),
                task.getId(),
                UUID.randomUUID(),
                creatorId,
                TaskStatus.TODO,
                TaskStatus.IN_PROGRESS,
                java.time.Instant.now()
        );

        notificationService.onTaskStatusChanged(event);

        verify(notificationRepository).save(any(Notification.class));
        verify(realtimePushService).sendNotificationToUser(eq(recipient.getEmail()), any(NotificationResponse.class));
    }

    @Test
    @DisplayName("Should mark notification as read when owned by authenticated user")
    void shouldMarkNotificationAsRead() {
        UUID notifId = UUID.randomUUID();
        Notification notification = Notification.builder()
                .id(notifId)
                .recipient(recipient)
                .type(NotificationType.TASK_ASSIGNED)
                .title("Assigned")
                .message("Message")
                .isRead(false)
                .build();

        when(userRepository.findByEmail(recipient.getEmail())).thenReturn(Optional.of(recipient));
        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(notifId, recipientAuth);

        assertThat(response.isRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("Should reject markAsRead if user does not own notification")
    void shouldRejectMarkAsReadForOtherUser() {
        UUID notifId = UUID.randomUUID();
        Notification notification = Notification.builder()
                .id(notifId)
                .recipient(creator) // owned by Bob, requested by Alice
                .type(NotificationType.TASK_ASSIGNED)
                .title("Assigned")
                .message("Message")
                .isRead(false)
                .build();

        when(userRepository.findByEmail(recipient.getEmail())).thenReturn(Optional.of(recipient));
        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(notifId, recipientAuth))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not own this notification");
    }

    @Test
    @DisplayName("Should delete notification when owned by authenticated user")
    void shouldDeleteNotificationWhenOwned() {
        UUID notifId = UUID.randomUUID();
        Notification notification = Notification.builder()
                .id(notifId)
                .recipient(recipient)
                .title("Assigned")
                .message("Message")
                .build();

        when(userRepository.findByEmail(recipient.getEmail())).thenReturn(Optional.of(recipient));
        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notification));

        notificationService.deleteNotification(notifId, recipientAuth);

        verify(notificationRepository).deleteByIdAndRecipient(notifId, recipient);
    }

    @Test
    @DisplayName("Should reject deleting notification owned by another user")
    void shouldRejectDeleteNotificationOwnedByOtherUser() {
        UUID notifId = UUID.randomUUID();
        Notification notification = Notification.builder()
                .id(notifId)
                .recipient(creator) // owned by Bob
                .title("Assigned")
                .message("Message")
                .build();

        when(userRepository.findByEmail(recipient.getEmail())).thenReturn(Optional.of(recipient));
        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.deleteNotification(notifId, recipientAuth))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not own this notification");

        verify(notificationRepository, never()).deleteByIdAndRecipient(any(), any());
    }

    @Test
    @DisplayName("Should bulk clear notifications for current user only")
    void shouldBulkClearNotificationsForCurrentUser() {
        when(userRepository.findByEmail(recipient.getEmail())).thenReturn(Optional.of(recipient));
        when(notificationRepository.deleteAllByRecipient(recipient)).thenReturn(5);

        int count = notificationService.deleteAllNotifications(recipientAuth);

        assertThat(count).isEqualTo(5);
        verify(notificationRepository).deleteAllByRecipient(recipient);
    }
}
