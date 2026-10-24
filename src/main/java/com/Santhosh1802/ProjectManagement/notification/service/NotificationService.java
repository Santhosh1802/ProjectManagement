package com.Santhosh1802.ProjectManagement.notification.service;

import com.Santhosh1802.ProjectManagement.comment.event.TaskCommentCreatedEvent;
import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.Task;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.exception.notification.NotificationNotFoundException;
import com.Santhosh1802.ProjectManagement.exception.user.UserNotFoundException;
import com.Santhosh1802.ProjectManagement.notification.dto.response.NotificationResponse;
import com.Santhosh1802.ProjectManagement.notification.entity.Notification;
import com.Santhosh1802.ProjectManagement.notification.entity.NotificationType;
import com.Santhosh1802.ProjectManagement.notification.repository.NotificationRepository;
import com.Santhosh1802.ProjectManagement.projects.event.ProjectMemberAddedEvent;
import com.Santhosh1802.ProjectManagement.realtime.service.RealtimePushService;
import com.Santhosh1802.ProjectManagement.repository.ProjectMemberRepository;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.repository.TaskRepository;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.tasks.event.TaskCreatedEvent;
import com.Santhosh1802.ProjectManagement.tasks.event.TaskStatusChangedEvent;
import com.Santhosh1802.ProjectManagement.util.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final RealtimePushService realtimePushService;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            RealtimePushService realtimePushService
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.realtimePushService = realtimePushService;
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getMyNotifications(Authentication authentication, int page, int size) {
        User user = getCurrentUser(authentication);
        Page<Notification> paged = notificationRepository.findByRecipientOrderByCreatedAtDesc(
                user,
                PageRequest.of(page, size)
        );

        return new PageResponse<>(
                paged.getContent().stream().map(this::toResponse).toList(),
                paged.getNumber(),
                paged.getSize(),
                paged.getTotalElements(),
                paged.getTotalPages(),
                paged.isFirst(),
                paged.isLast()
        );
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found: " + notificationId));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not own this notification");
        }

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return toResponse(saved);
    }

    @Transactional
    public int markAllAsRead(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return notificationRepository.markAllAsReadByRecipient(user);
    }

    // ==========================================
    // Event-Driven Notification Generators
    // Rollback-safe via AFTER_COMMIT + REQUIRES_NEW
    // ==========================================

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onTaskCreated(TaskCreatedEvent event) {
        if (event.assignedToId() == null || event.assignedToId().equals(event.actorId())) {
            // No notification needed if unassigned or self-assigned
            return;
        }

        userRepository.findById(event.assignedToId()).ifPresent(recipient -> {
            String title = "New Task Assigned";
            String message = "You have been assigned to task: " + event.title();

            createAndDispatch(recipient, NotificationType.TASK_ASSIGNED, title, message,
                    event.taskId(), event.projectId(), event.actorId());
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onTaskStatusChanged(TaskStatusChangedEvent event) {
        taskRepository.findById(event.taskId()).ifPresent(task -> {
            // Notify task creator and/or assignee if not the actor
            User assignee = task.getAssignedTo();
            User creator = task.getCreatedBy();

            String title = "Task Status Updated";
            String message = String.format("Task '%s' status changed from %s to %s",
                    task.getTitle(), event.previousStatus(), event.newStatus());

            if (assignee != null && !assignee.getId().equals(event.actorId())) {
                createAndDispatch(assignee, NotificationType.TASK_STATUS_CHANGED, title, message,
                        task.getId(), event.projectId(), event.actorId());
            }

            if (creator != null && !creator.getId().equals(event.actorId())
                    && (assignee == null || !creator.getId().equals(assignee.getId()))) {
                createAndDispatch(creator, NotificationType.TASK_STATUS_CHANGED, title, message,
                        task.getId(), event.projectId(), event.actorId());
            }
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onTaskCommentCreated(TaskCommentCreatedEvent event) {
        taskRepository.findById(event.taskId()).ifPresent(task -> {
            User assignee = task.getAssignedTo();
            User creator = task.getCreatedBy();

            String title = "New Comment on Task";
            String message = "A new comment was posted on task: " + task.getTitle();

            if (assignee != null && !assignee.getId().equals(event.authorId())) {
                createAndDispatch(assignee, NotificationType.TASK_COMMENT_ADDED, title, message,
                        task.getId(), event.projectId(), event.authorId());
            }

            if (creator != null && !creator.getId().equals(event.authorId())
                    && (assignee == null || !creator.getId().equals(assignee.getId()))) {
                createAndDispatch(creator, NotificationType.TASK_COMMENT_ADDED, title, message,
                        task.getId(), event.projectId(), event.authorId());
            }
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onProjectMemberAdded(ProjectMemberAddedEvent event) {
        userRepository.findById(event.userId()).ifPresent(member -> {
            projectRepository.findById(event.projectId()).ifPresent(project -> {
                String title = "Added to Project";
                String message = String.format("You have been added to project '%s' as %s",
                        project.getName(), event.role());

                createAndDispatch(member, NotificationType.PROJECT_MEMBER_ADDED, title, message,
                        null, event.projectId(), null);
            });
        });
    }

    private void createAndDispatch(
            User recipient,
            NotificationType type,
            String title,
            String message,
            UUID taskId,
            UUID projectId,
            UUID actorId
    ) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .message(message)
                .taskId(taskId)
                .projectId(projectId)
                .actorId(actorId)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = toResponse(saved);

        // Push through STOMP directly to recipient's private destination
        realtimePushService.sendNotificationToUser(recipient.getEmail(), response);
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + authentication.getName()));
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getRecipient() != null ? n.getRecipient().getId() : null,
                n.getType(),
                n.getTitle(),
                n.getMessage(),
                n.getTaskId(),
                n.getProjectId(),
                n.getActorId(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
