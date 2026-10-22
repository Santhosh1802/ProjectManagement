package com.Santhosh1802.ProjectManagement.tasks.event;

import com.Santhosh1802.ProjectManagement.shared.event.DomainEvent;
import com.Santhosh1802.ProjectManagement.util.TaskPriority;
import com.Santhosh1802.ProjectManagement.util.TaskStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable event published when a task's core details are updated.
 */
public record TaskUpdatedEvent(
        UUID eventId,
        UUID taskId,
        UUID projectId,
        UUID actorId,
        String title,
        TaskStatus status,
        TaskPriority priority,
        UUID assignedToId,
        Instant dueDate,
        Instant occurredAt
) implements DomainEvent {

    public static TaskUpdatedEvent of(
            UUID taskId,
            UUID projectId,
            UUID actorId,
            String title,
            TaskStatus status,
            TaskPriority priority,
            UUID assignedToId,
            Instant dueDate
    ) {
        return new TaskUpdatedEvent(
                UUID.randomUUID(),
                taskId,
                projectId,
                actorId,
                title,
                status,
                priority,
                assignedToId,
                dueDate,
                Instant.now()
        );
    }
}
