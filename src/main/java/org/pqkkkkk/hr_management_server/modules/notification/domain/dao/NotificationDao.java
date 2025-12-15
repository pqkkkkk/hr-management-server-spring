package org.pqkkkkk.hr_management_server.modules.notification.domain.dao;

import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Notification;

public interface NotificationDao {
    public Notification createNotification(Notification notification);
    public Notification getNotificationById(String notificationId);
}
