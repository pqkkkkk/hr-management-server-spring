package org.pqkkkkk.hr_management_server.modules.notification.domain.service;

import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Notification;
import org.pqkkkkk.hr_management_server.modules.notification.domain.filter.FilterCriteria.NotificationFilter;
import org.springframework.data.domain.Page;

public interface NotificationQueryService {
    Page<Notification> getNotifications(NotificationFilter filter);
    Long getUnreadCount(String recipientId);
}
