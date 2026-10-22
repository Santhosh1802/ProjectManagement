package com.Santhosh1802.ProjectManagement.shared.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Common marker contract for domain events across the application.
 */
public interface DomainEvent {
    UUID eventId();
    Instant occurredAt();
}
