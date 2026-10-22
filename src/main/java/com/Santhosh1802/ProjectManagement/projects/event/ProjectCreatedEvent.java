package com.Santhosh1802.ProjectManagement.projects.event;

import com.Santhosh1802.ProjectManagement.shared.event.DomainEvent;
import com.Santhosh1802.ProjectManagement.util.ProjectStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable event published when a new project is created.
 */
public record ProjectCreatedEvent(
        UUID eventId,
        UUID projectId,
        String name,
        UUID ownerId,
        ProjectStatus status,
        Instant startDate,
        Instant dueDate,
        Instant occurredAt
) implements DomainEvent {

    public static ProjectCreatedEvent of(
            UUID projectId,
            String name,
            UUID ownerId,
            ProjectStatus status,
            Instant startDate,
            Instant dueDate
    ) {
        return new ProjectCreatedEvent(
                UUID.randomUUID(),
                projectId,
                name,
                ownerId,
                status,
                startDate,
                dueDate,
                Instant.now()
        );
    }
}
