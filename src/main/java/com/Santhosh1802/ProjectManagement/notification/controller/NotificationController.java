package com.Santhosh1802.ProjectManagement.notification.controller;

import com.Santhosh1802.ProjectManagement.notification.dto.response.NotificationResponse;
import com.Santhosh1802.ProjectManagement.notification.service.NotificationService;
import com.Santhosh1802.ProjectManagement.util.ApiResponse;
import com.Santhosh1802.ProjectManagement.util.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getMyNotifications(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page cannot be negative") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be at least 1") @Max(value = 100, message = "Size cannot exceed 100") int size,
            Authentication authentication
    ) {
        PageResponse<NotificationResponse> result = notificationService.getMyNotifications(authentication, page, size);
        return ResponseEntity.ok(new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Notifications retrieved successfully",
                result
        ));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(Authentication authentication) {
        long count = notificationService.getUnreadCount(authentication);
        return ResponseEntity.ok(new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Unread count retrieved",
                Map.of("unreadCount", count)
        ));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        NotificationResponse updated = notificationService.markAsRead(id, authentication);
        return ResponseEntity.ok(new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "Notification marked as read",
                updated
        ));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllAsRead(Authentication authentication) {
        int updatedCount = notificationService.markAllAsRead(authentication);
        return ResponseEntity.ok(new ApiResponse<>(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                "All notifications marked as read",
                Map.of("updatedCount", updatedCount)
        ));
    }
}
