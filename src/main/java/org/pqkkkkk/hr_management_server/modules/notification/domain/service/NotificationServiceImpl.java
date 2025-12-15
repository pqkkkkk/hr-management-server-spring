package org.pqkkkkk.hr_management_server.modules.notification.domain.service;

import java.util.List;
import java.util.Map;

import org.pqkkkkk.hr_management_server.modules.notification.domain.dao.NotificationDao;
import org.pqkkkkk.hr_management_server.modules.notification.domain.dao.NotificationTemplateDao;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Notification;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.NotificationContext;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationType;
import org.pqkkkkk.hr_management_server.modules.notification.domain.service.delivery.NotificationDelivery;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationDao notificationDao;
    private final NotificationTemplateDao notificationTemplateDao;
    private final List<NotificationDelivery> deliveryChannels;

    public NotificationServiceImpl(NotificationDao notificationDao,
        NotificationTemplateDao notificationTemplateDao,
        List<NotificationDelivery> deliveryChannels) {

        this.notificationDao = notificationDao;
        this.notificationTemplateDao = notificationTemplateDao;
        this.deliveryChannels = deliveryChannels;
    }

    @Override
    @Transactional
    public Notification createNotification(NotificationContext context) {
        String template = notificationTemplateDao.getTemplateByType(context.getType().name());

        // Generate title from notification type
        String title = generateTitle(context.getType());

        // Modify template with context data
        String message = formatTemplate(template, context.getTemplateData());

        // Create Notification entity and persist it
        Notification notification = Notification.builder()
            .title(title)
            .type(context.getType())
            .referenceType(context.getReferenceType())
            .referenceId(context.getReferenceId())
            .recipientId(context.getRecipientId())
            .message(message)
            .isRead(false)
            .build();

        return notificationDao.createNotification(notification);
    }

    @Override
    @Transactional
    public void markAsRead(String notificationId) {
        Notification notification = notificationDao.getNotificationById(notificationId);

        if(notification == null) {
            throw new IllegalArgumentException("Notification not found with ID: " + notificationId);
        }

        notification.setIsRead(true);
    }

    @Override
    public void sendNotification(Notification notification) {

        for (NotificationDelivery delivery : deliveryChannels) {
                delivery.deliver(notification);
        }
    }

    private String formatTemplate(String template, Map<String, Object> data) {
        // Simple template formatting: "Hello {name}" with data {name: "John"} -> "Hello John"
        String result = template;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue().toString());
        }
        return result;
    }

    private String generateTitle(NotificationType type) {
        return switch (type) {
            case REQUEST_APPROVED -> "Yêu cầu đã được phê duyệt";
            case REQUEST_REJECTED -> "Yêu cầu bị từ chối";
            case REQUEST_CREATED -> "Yêu cầu mới được tạo";
            case REQUEST_EXPIRED -> "Yêu cầu đã hết hạn";
        };
    }

}
