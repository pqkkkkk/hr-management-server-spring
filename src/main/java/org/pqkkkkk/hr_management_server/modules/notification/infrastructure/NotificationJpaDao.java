package org.pqkkkkk.hr_management_server.modules.notification.infrastructure;

import org.pqkkkkk.hr_management_server.modules.notification.domain.dao.NotificationDao;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Notification;
import org.pqkkkkk.hr_management_server.modules.notification.infrastructure.dao.NotificationRepository;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationJpaDao implements NotificationDao {
    private final NotificationRepository notificationRepository;

    public NotificationJpaDao(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    @Override
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }
    @Override
    public Notification getNotificationById(String notificationId) {
        return notificationRepository.findById(notificationId).orElse(null);
    }

}
