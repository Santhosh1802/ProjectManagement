package com.Santhosh1802.ProjectManagement.tasks.event;

import com.Santhosh1802.ProjectManagement.shared.event.DomainEvent;
import com.Santhosh1802.ProjectManagement.util.TaskPriority;
import com.Santhosh1802.ProjectManagement.util.TaskStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable event published when a task is successfully created.
 */
public record TaskCreatedEvent(
        UUID eventId,
        UUID taskId,
        UUID projectId,
        UUID actorId,
        UUID assignedToId,
        String title,
        TaskStatus status,
        TaskPriority priority,
        Instant occurredAt
) implements DomainEvent {

    public static TaskCreatedEvent of(
            UUID taskId,
            UUID projectId,
            UUID actorId,
            UUID assignedToId,
            String title,
            TaskStatus status,
            TaskPriority priority
    ) {
        return new TaskCreatedEvent(
                UUID.randomUUID(),
                taskId,
                projectId,
                actorId,
                assignedToId,
                title,
                status,
                priority,
                Instant.now()
        );
    }
}
