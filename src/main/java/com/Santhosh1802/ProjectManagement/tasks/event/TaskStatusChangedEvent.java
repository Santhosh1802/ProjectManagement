package com.Santhosh1802.ProjectManagement.tasks.event;

import com.Santhosh1802.ProjectManagement.shared.event.DomainEvent;
import com.Santhosh1802.ProjectManagement.util.TaskStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable event published when a task's status changes.
 */
public record TaskStatusChangedEvent(
        UUID eventId,
        UUID taskId,
        UUID projectId,
        UUID actorId,
        TaskStatus previousStatus,
        TaskStatus newStatus,
        Instant occurredAt
) implements DomainEvent {

    public static TaskStatusChangedEvent of(
            UUID taskId,
            UUID projectId,
            UUID actorId,
            TaskStatus previousStatus,
            TaskStatus newStatus
    ) {
        return new TaskStatusChangedEvent(
                UUID.randomUUID(),
                taskId,
                projectId,
                actorId,
                previousStatus,
                newStatus,
                Instant.now()
        );
    }
}
