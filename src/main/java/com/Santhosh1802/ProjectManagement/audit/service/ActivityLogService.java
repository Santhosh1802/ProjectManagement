package com.Santhosh1802.ProjectManagement.audit.service;

import com.Santhosh1802.ProjectManagement.audit.dto.response.ActivityLogResponse;
import com.Santhosh1802.ProjectManagement.audit.entity.ActivityLog;
import com.Santhosh1802.ProjectManagement.audit.repository.ActivityLogRepository;
import com.Santhosh1802.ProjectManagement.comment.event.TaskCommentCreatedEvent;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.projects.event.ProjectCreatedEvent;
import com.Santhosh1802.ProjectManagement.projects.event.ProjectMemberAddedEvent;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.tasks.event.TaskCreatedEvent;
import com.Santhosh1802.ProjectManagement.tasks.event.TaskStatusChangedEvent;
import com.Santhosh1802.ProjectManagement.tasks.event.TaskUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ActivityLogService {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogService.class);

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository, UserRepository userRepository) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getTaskActivity(UUID taskId) {
        return activityLogRepository.findByTaskIdOrderByCreatedAtDesc(taskId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getProjectActivity(UUID projectId) {
        return activityLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onTaskStatusChanged(TaskStatusChangedEvent event) {
        log.debug("Recording activity for TaskStatusChangedEvent: {}", event.eventId());
        User actor = resolveActor(event.actorId());
        String actorName = actor != null ? actor.getFirstName() + (actor.getLastName() != null ? " " + actor.getLastName() : "") : "System";
        String actorEmail = actor != null ? actor.getEmail() : null;

        String details = "Status changed from " + event.previousStatus() + " to " + event.newStatus();

        ActivityLog logEntry = ActivityLog.builder()
                .taskId(event.taskId())
                .projectId(event.projectId())
                .actorId(event.actorId())
                .actorName(actorName)
                .actorEmail(actorEmail)
                .actionType("TASK_STATUS_CHANGED")
                .details(details)
                .build();

        activityLogRepository.save(logEntry);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onTaskCreated(TaskCreatedEvent event) {
        log.debug("Recording activity for TaskCreatedEvent: {}", event.eventId());
        User actor = resolveActor(event.actorId());
        String actorName = actor != null ? actor.getFirstName() + (actor.getLastName() != null ? " " + actor.getLastName() : "") : "System";
        String actorEmail = actor != null ? actor.getEmail() : null;

        String details = "Task created with title '" + event.title() + "' and priority " + event.priority();

        ActivityLog logEntry = ActivityLog.builder()
                .taskId(event.taskId())
                .projectId(event.projectId())
                .actorId(event.actorId())
                .actorName(actorName)
                .actorEmail(actorEmail)
                .actionType("TASK_CREATED")
                .details(details)
                .build();

        activityLogRepository.save(logEntry);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onTaskUpdated(TaskUpdatedEvent event) {
        log.debug("Recording activity for TaskUpdatedEvent: {}", event.eventId());
        User actor = resolveActor(event.actorId());
        String actorName = actor != null ? actor.getFirstName() + (actor.getLastName() != null ? " " + actor.getLastName() : "") : "System";
        String actorEmail = actor != null ? actor.getEmail() : null;

        String details = "Task updated: title '" + event.title() + "', status " + event.status() + ", priority " + event.priority();

        ActivityLog logEntry = ActivityLog.builder()
                .taskId(event.taskId())
                .projectId(event.projectId())
                .actorId(event.actorId())
                .actorName(actorName)
                .actorEmail(actorEmail)
                .actionType("TASK_UPDATED")
                .details(details)
                .build();

        activityLogRepository.save(logEntry);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onTaskCommentCreated(TaskCommentCreatedEvent event) {
        log.debug("Recording activity for TaskCommentCreatedEvent: {}", event.eventId());
        User actor = resolveActor(event.authorId());
        String actorName = actor != null ? actor.getFirstName() + (actor.getLastName() != null ? " " + actor.getLastName() : "") : "User";
        String actorEmail = actor != null ? actor.getEmail() : null;

        String details = "Posted a comment";

        ActivityLog logEntry = ActivityLog.builder()
                .taskId(event.taskId())
                .projectId(event.projectId())
                .actorId(event.authorId())
                .actorName(actorName)
                .actorEmail(actorEmail)
                .actionType("TASK_COMMENT_CREATED")
                .details(details)
                .build();

        activityLogRepository.save(logEntry);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onProjectCreated(ProjectCreatedEvent event) {
        log.debug("Recording activity for ProjectCreatedEvent: {}", event.eventId());
        User actor = resolveActor(event.ownerId());
        String actorName = actor != null ? actor.getFirstName() + (actor.getLastName() != null ? " " + actor.getLastName() : "") : "Owner";
        String actorEmail = actor != null ? actor.getEmail() : null;

        String details = "Project '" + event.name() + "' created";

        ActivityLog logEntry = ActivityLog.builder()
                .taskId(null)
                .projectId(event.projectId())
                .actorId(event.ownerId())
                .actorName(actorName)
                .actorEmail(actorEmail)
                .actionType("PROJECT_CREATED")
                .details(details)
                .build();

        activityLogRepository.save(logEntry);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onProjectMemberAdded(ProjectMemberAddedEvent event) {
        log.debug("Recording activity for ProjectMemberAddedEvent: {}", event.eventId());
        User member = resolveActor(event.userId());
        String memberName = member != null ? member.getFirstName() + (member.getLastName() != null ? " " + member.getLastName() : "") : "User";

        String details = "Member " + memberName + " added as " + event.role();

        ActivityLog logEntry = ActivityLog.builder()
                .taskId(null)
                .projectId(event.projectId())
                .actorId(event.userId())
                .actorName(memberName)
                .actorEmail(member != null ? member.getEmail() : null)
                .actionType("PROJECT_MEMBER_ADDED")
                .details(details)
                .build();

        activityLogRepository.save(logEntry);
    }

    private User resolveActor(UUID actorId) {
        if (actorId == null) {
            return null;
        }
        return userRepository.findById(actorId).orElse(null);
    }

    private ActivityLogResponse toResponse(ActivityLog log) {
        return new ActivityLogResponse(
                log.getId(),
                log.getTaskId(),
                log.getProjectId(),
                log.getActorId(),
                log.getActorName(),
                log.getActorEmail(),
                log.getActionType(),
                log.getDetails(),
                log.getCreatedAt()
        );
    }
}
