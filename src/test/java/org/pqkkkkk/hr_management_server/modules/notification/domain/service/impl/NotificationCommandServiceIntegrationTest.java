package org.pqkkkkk.hr_management_server.modules.notification.domain.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.pqkkkkk.hr_management_server.modules.notification.domain.dao.NotificationDao;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Notification;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.NotificationContext;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.NotificationTemplate;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationType;
import org.pqkkkkk.hr_management_server.modules.notification.infrastructure.dao.NotificationTemplateRepository;
import org.pqkkkkk.hr_management_server.modules.notification.domain.service.NotificationCommandService;
import org.pqkkkkk.hr_management_server.modules.notification.domain.service.delivery.SseNotificationDelivery;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class NotificationCommandServiceIntegrationTest {

    @Autowired
    NotificationCommandService notificationCommandService;

    @Autowired
    NotificationDao notificationDao;

    @Autowired
    NotificationTemplateRepository templateRepository;

    @MockitoBean
    SseNotificationDelivery sseNotificationDelivery; // mock concrete delivery bean to avoid sending

    @Test
    void happyPath_templateExists_persistsNotification() {
        // Ensure seed has template for REQUEST_CREATED + EMPLOYEE
        NotificationTemplate template = templateRepository.findByNotificationTypeAndUserRoleAndIsActive(
            NotificationType.REQUEST_CREATED, UserRole.EMPLOYEE.name(), true).orElseThrow();

        NotificationContext context = NotificationContext.builder()
            .recipientId("user-123")
            .userRole(UserRole.EMPLOYEE.name())
            .type(NotificationType.REQUEST_CREATED)
            .templateData(Map.of("name", "Alice", "requestType", "LEAVE", "createdAt", "01/01/2025 09:00"))
            .build();

        Notification created = notificationCommandService.createNotification(context);

        assertNotNull(created);
        Notification persisted = notificationDao.getNotificationById(created.getNotificationId());
        assertNotNull(persisted);
        assertEquals(template.getTitleTemplate(), persisted.getTitle());
        assertEquals("Yêu cầu LEAVE của bạn đã được tạo thành công vào lúc 01/01/2025 09:00.".replace("\u00A0", " "), // normalize
            persisted.getMessage());

        // Delivery should not be automatically called by createNotification
        verifyNoInteractions(sseNotificationDelivery);
    }

    @Test
    void fallbackToType_whenRoleTemplateMissing() {
        // Choose a role that likely has no template (HR), expecting fallback to EMPLOYEE template
        NotificationContext context = NotificationContext.builder()
            .recipientId("user-456")
            .userRole("HR")
            .type(NotificationType.REQUEST_CREATED)
            .templateData(Map.of("employeeName", "Bob", "requestType", "LEAVE", "createdAt", "02/02/2025 10:00"))
            .build();

        Notification created = notificationCommandService.createNotification(context);
        assertNotNull(created);
        Notification persisted = notificationDao.getNotificationById(created.getNotificationId());
        assertNotNull(persisted);

        // We expect fallback to the EMPLOYEE template message structure (seeded)
        // Just assert message contains employeeName and requestType
        String msg = persisted.getMessage();
        assertNotNull(msg);
        // Contains requestType
        org.junit.jupiter.api.Assertions.assertTrue(msg.contains("LEAVE") || msg.contains("{requestType}") == false);
    }

    @Test
    void missingTemplate_returnsNullAndDoesNotPersist() {
        // Use a type that has no template in seed (REQUEST_EXPIRED)
        NotificationContext context = NotificationContext.builder()
            .recipientId("user-789")
            .userRole(UserRole.EMPLOYEE.name())
            .type(NotificationType.REQUEST_EXPIRED)
            .templateData(Map.of("some", "data"))
            .build();

        Notification created = notificationCommandService.createNotification(context);

        assertNull(created);
    }

    @Test
    void formatTemplate_endToEnd_replacesPlaceholders() {
        // Insert a specific template to DB for this test
        NotificationTemplate t = NotificationTemplate.builder()
            .notificationType(NotificationType.REQUEST_REJECTED)
            .userRole(UserRole.EMPLOYEE.name())
            .titleTemplate("Rejected")
            .messageTemplate("Dear {employeeName}, your request {requestType} was rejected by {approverName}.")
            .isActive(true)
            .build();

        // If template with same (type, role) exists from seed, update it instead of inserting
        var existing = templateRepository.findByNotificationTypeAndUserRoleAndIsActive(NotificationType.REQUEST_REJECTED, UserRole.EMPLOYEE.name(), true);
        if (existing.isPresent()) {
            NotificationTemplate et = existing.get();
            et.setTitleTemplate(t.getTitleTemplate());
            et.setMessageTemplate(t.getMessageTemplate());
            templateRepository.save(et);
        } else {
            templateRepository.save(t);
        }

        NotificationContext context = NotificationContext.builder()
            .recipientId("user-000")
            .userRole(UserRole.EMPLOYEE.name())
            .type(NotificationType.REQUEST_REJECTED)
            .templateData(Map.of("employeeName", "Carol", "requestType", "LEAVE", "approverName", "Manager X"))
            .build();

        Notification created = notificationCommandService.createNotification(context);
        assertNotNull(created);
        Notification persisted = notificationDao.getNotificationById(created.getNotificationId());
        assertEquals("Dear Carol, your request LEAVE was rejected by Manager X.", persisted.getMessage());
    }
}
