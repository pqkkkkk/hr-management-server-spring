package org.pqkkkkk.hr_management_server.modules.notification.domain.service;

import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Notification;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.NotificationContext;

public interface NotificationService {
    public Notification createNotification(NotificationContext context);
    public void markAsRead(String notificationId);
    public void sendNotification(Notification context);
}
