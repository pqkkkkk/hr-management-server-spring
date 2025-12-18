package org.pqkkkkk.hr_management_server.modules.notification.domain.service.impl;

import java.util.List;
import java.util.Map;

import org.pqkkkkk.hr_management_server.modules.notification.domain.dao.NotificationDao;
import org.pqkkkkk.hr_management_server.modules.notification.domain.dao.NotificationTemplateDao;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Notification;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.NotificationContext;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.NotificationTemplate;
import org.pqkkkkk.hr_management_server.modules.notification.domain.service.NotificationCommandService;
import org.pqkkkkk.hr_management_server.modules.notification.domain.service.delivery.NotificationDelivery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationCommandServiceImpl implements NotificationCommandService {
    private final NotificationDao notificationDao;
    private final NotificationTemplateDao notificationTemplateDao;
    private final List<NotificationDelivery> deliveryChannels;

    public NotificationCommandServiceImpl(NotificationDao notificationDao,
        @Qualifier("notificationTemplateJpaDao") NotificationTemplateDao notificationTemplateDao,
        List<NotificationDelivery> deliveryChannels) {

        this.notificationDao = notificationDao;
        this.notificationTemplateDao = notificationTemplateDao;
        this.deliveryChannels = deliveryChannels;
    }

    @Override
    @Transactional
    public Notification createNotification(NotificationContext context) {
        NotificationTemplate template = notificationTemplateDao.getTemplateByTypeAndUserRole(
            context.getType().name(),
            context.getUserRole()
        );

        if (template == null) {
            // Graceful behavior: if there is no template configured, skip creating notification
            log.debug("No active notification template found for type: {} and user role: {}", context.getType(), context.getUserRole());
            return null;
        }

        // Modify template with context data
        String message = formatTemplate(template.getMessageTemplate(), context.getTemplateData());

        // Create Notification entity and persist it
        Notification notification = Notification.builder()
            .title(template.getTitleTemplate())
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
    @Transactional
    public void markAllAsRead(String recipientId) {
        if (recipientId == null || recipientId.isBlank()) {
            throw new IllegalArgumentException("Recipient ID is required");
        }

        Integer updatedCount = notificationDao.markAllAsRead(recipientId);
        
        if (updatedCount == 0) {
            // No notifications to mark as read - this is OK, not an error
            return;
        }
    }

    @Override
    public void sendNotification(Notification notification) {
        for (NotificationDelivery delivery : deliveryChannels) {
            delivery.deliver(notification);
        }
    }

    private String formatTemplate(String template, Map<String, Object> data) {
        if (template == null) return "";

        // Find placeholders like {key}
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{([^}]+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(template);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = (data == null) ? null : data.get(key);
            if (value == null) {
                // graceful: replace missing/ null with empty string and warn
                matcher.appendReplacement(sb, "");
                log.warn("Missing template key '{}' while formatting notification", key);
            } else {
                // escape replacement to avoid group references
                String replacement = java.util.regex.Matcher.quoteReplacement(value.toString());
                matcher.appendReplacement(sb, replacement);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
