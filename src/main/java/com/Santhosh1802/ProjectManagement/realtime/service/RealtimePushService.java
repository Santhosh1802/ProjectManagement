package com.Santhosh1802.ProjectManagement.realtime.service;

import com.Santhosh1802.ProjectManagement.notification.dto.response.NotificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RealtimePushService {

    private static final Logger log = LoggerFactory.getLogger(RealtimePushService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public RealtimePushService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sends a private real-time notification payload directly to the authenticated user's queue.
     * The client subscribes to /user/queue/notifications.
     *
     * @param recipientUsername The username/email of the recipient
     * @param notification The notification DTO payload
     */
    public void sendNotificationToUser(String recipientUsername, NotificationResponse notification) {
        if (recipientUsername == null || recipientUsername.isBlank()) {
            log.warn("Cannot send realtime notification: recipientUsername is empty");
            return;
        }
        try {
            log.debug("Pushing realtime notification to user {}: {}", recipientUsername, notification.id());
            messagingTemplate.convertAndSendToUser(
                    recipientUsername,
                    "/queue/notifications",
                    notification
            );
        } catch (Exception e) {
            log.error("Failed to push realtime notification to user {}: {}", recipientUsername, e.getMessage());
        }
    }
}
