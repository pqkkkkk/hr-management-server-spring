package org.pqkkkkk.hr_management_server.modules.notification.domain.entity;

public class Enums {
    public enum NotificationChannel {
        SSE
    }

    public enum NotificationReferenceType {
        REQUEST,
        REWARD,
        ACTIVITY
    }

    public enum NotificationType {
        REQUEST_APPROVED,
        REQUEST_REJECTED,
        REQUEST_CREATED,
        REQUEST_EXPIRED
    }
    public enum NotificationSortingField {
        CREATED_AT,
        TYPE,
        IS_READ
    }
}
