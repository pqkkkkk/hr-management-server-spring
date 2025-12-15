package org.pqkkkkk.hr_management_server.modules.notification.controller.http.dto;

import java.time.LocalDateTime;

import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Notification;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationReferenceType;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationType;

public class DTO {
    /**
     * NotificationDTO - Data Transfer Object for Notification entity
     * 
     * Used for API responses to return notification data to clients.
     * Includes all essential notification information.
     */
    public record NotificationDTO(
        String notificationId,
        String title,
        String message,
        NotificationType type,
        NotificationReferenceType referenceType,
        String referenceId,
        Boolean isRead,
        LocalDateTime createdAt,
        String recipientId
    ) {
        /**
         * Convert DTO to Entity
         * Used when creating or updating notifications from API requests
         * 
         * @return Notification entity
         */
        public Notification toEntity() {
            return Notification.builder()
                .notificationId(notificationId)
                .title(title)
                .message(message)
                .type(type)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .isRead(isRead)
                .createdAt(createdAt)
                .recipientId(recipientId)
                .build();
        }

        /**
         * Convert Entity to DTO
         * Used when returning notification data from service layer to API layer
         * 
         * @param notification Notification entity
         * @return NotificationDTO
         */
        public static NotificationDTO fromEntity(Notification notification) {
            if (notification == null) {
                return null;
            }

            return new NotificationDTO(
                notification.getNotificationId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getReferenceType(),
                notification.getReferenceId(),
                notification.getIsRead(),
                notification.getCreatedAt(),
                notification.getRecipientId()
            );
        }
    }

    /**
     * NotificationSummaryDTO - Lightweight version for list views
     * 
     * Contains only essential fields for notification lists/summaries
     * to reduce payload size when fetching multiple notifications
     */
    public record NotificationSummaryDTO(
        String notificationId,
        String title,
        NotificationType type,
        Boolean isRead,
        LocalDateTime createdAt
    ) {
        /**
         * Convert Entity to Summary DTO
         * 
         * @param notification Notification entity
         * @return NotificationSummaryDTO
         */
        public static NotificationSummaryDTO fromEntity(Notification notification) {
            if (notification == null) {
                return null;
            }

            return new NotificationSummaryDTO(
                notification.getNotificationId(),
                notification.getTitle(),
                notification.getType(),
                notification.getIsRead(),
                notification.getCreatedAt()
            );
        }
    }
}
