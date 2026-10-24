package com.Santhosh1802.ProjectManagement.notification.dto.response;

import com.Santhosh1802.ProjectManagement.notification.entity.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID recipientId,
        NotificationType type,
        String title,
        String message,
        UUID taskId,
        UUID projectId,
        UUID actorId,
        boolean isRead,
        Instant createdAt
) {
}
