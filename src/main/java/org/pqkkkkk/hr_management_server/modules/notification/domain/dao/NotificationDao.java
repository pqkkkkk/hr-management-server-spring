package org.pqkkkkk.hr_management_server.modules.notification.domain.dao;

import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Notification;
import org.pqkkkkk.hr_management_server.modules.notification.domain.filter.FilterCriteria.NotificationFilter;
import org.springframework.data.domain.Page;

public interface NotificationDao {
    // Command methods
    public Notification createNotification(Notification notification);
    public Integer markAllAsRead(String recipientId);
    
    // Query methods
    public Notification getNotificationById(String notificationId);
    public Page<Notification> getNotifications(NotificationFilter filter);
    public Long countUnreadNotifications(String recipientId);
}
