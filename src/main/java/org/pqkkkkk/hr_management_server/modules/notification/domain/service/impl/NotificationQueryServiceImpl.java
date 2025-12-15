package org.pqkkkkk.hr_management_server.modules.notification.domain.service.impl;

import org.pqkkkkk.hr_management_server.modules.notification.domain.dao.NotificationDao;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Notification;
import org.pqkkkkk.hr_management_server.modules.notification.domain.filter.FilterCriteria.NotificationFilter;
import org.pqkkkkk.hr_management_server.modules.notification.domain.service.NotificationQueryService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class NotificationQueryServiceImpl implements NotificationQueryService {
    private final NotificationDao notificationDao;

    public NotificationQueryServiceImpl(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    @Override
    public Page<Notification> getNotifications(NotificationFilter filter) {
        // Validate recipientId is required
        if (filter.recipientId() == null || filter.recipientId().isBlank()) {
            throw new IllegalArgumentException("Recipient ID is required");
        }

        return notificationDao.getNotifications(filter);
    }

    @Override
    public Long getUnreadCount(String recipientId) {
        if (recipientId == null || recipientId.isBlank()) {
            throw new IllegalArgumentException("Recipient ID is required");
        }

        return notificationDao.countUnreadNotifications(recipientId);
    }
}
