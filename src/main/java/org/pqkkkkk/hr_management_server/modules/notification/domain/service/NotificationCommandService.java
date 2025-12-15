package org.pqkkkkk.hr_management_server.modules.notification.domain.service;

import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Notification;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.NotificationContext;

public interface NotificationCommandService {
    Notification createNotification(NotificationContext context);
    void markAsRead(String notificationId);
    void markAllAsRead(String recipientId);
    void sendNotification(Notification notification);
}
