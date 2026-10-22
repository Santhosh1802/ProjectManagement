package com.Santhosh1802.ProjectManagement.audit.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ActivityLogResponse(
        UUID id,
        UUID taskId,
        UUID projectId,
        UUID actorId,
        String actorName,
        String actorEmail,
        String actionType,
        String details,
        Instant createdAt
) {
}
