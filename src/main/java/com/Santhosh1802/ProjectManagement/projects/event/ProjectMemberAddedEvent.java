package com.Santhosh1802.ProjectManagement.projects.event;

import com.Santhosh1802.ProjectManagement.shared.event.DomainEvent;
import com.Santhosh1802.ProjectManagement.util.ProjectRole;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable event published when a member is added to a project.
 */
public record ProjectMemberAddedEvent(
        UUID eventId,
        UUID membershipId,
        UUID projectId,
        UUID userId,
        ProjectRole role,
        Instant occurredAt
) implements DomainEvent {

    public static ProjectMemberAddedEvent of(
            UUID membershipId,
            UUID projectId,
            UUID userId,
            ProjectRole role
    ) {
        return new ProjectMemberAddedEvent(
                UUID.randomUUID(),
                membershipId,
                projectId,
                userId,
                role,
                Instant.now()
        );
    }
}
