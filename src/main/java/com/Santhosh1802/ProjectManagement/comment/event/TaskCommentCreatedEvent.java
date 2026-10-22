package com.Santhosh1802.ProjectManagement.comment.event;

import com.Santhosh1802.ProjectManagement.shared.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable event published when a new comment is posted on a task.
 */
public record TaskCommentCreatedEvent(
        UUID eventId,
        UUID commentId,
        UUID taskId,
        UUID projectId,
        UUID authorId,
        Instant occurredAt
) implements DomainEvent {

    public static TaskCommentCreatedEvent of(
            UUID commentId,
            UUID taskId,
            UUID projectId,
            UUID authorId
    ) {
        return new TaskCommentCreatedEvent(
                UUID.randomUUID(),
                commentId,
                taskId,
                projectId,
                authorId,
                Instant.now()
        );
    }
}
