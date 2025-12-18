package org.pqkkkkk.hr_management_server.modules.notification.controller.event_listener;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.NotificationContext;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationReferenceType;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationType;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Notification;
import org.pqkkkkk.hr_management_server.modules.notification.domain.service.NotificationCommandService;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserRole;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.request.domain.event.RequestApprovedEvent;
import org.pqkkkkk.hr_management_server.modules.request.domain.event.RequestCreatedEvent;
import org.pqkkkkk.hr_management_server.modules.request.domain.event.RequestRejectedEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class RequestEventListener {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private final NotificationCommandService notificationCommandService;

    public RequestEventListener(NotificationCommandService notificationCommandService) {
        this.notificationCommandService = notificationCommandService;
    }

    @EventListener(RequestCreatedEvent.class)
    @Async
    void handleRequestCreatedEvent(RequestCreatedEvent event) {
        // Map domain event to notification context
        List<NotificationContext> contexts = createContextFromEvent(event);

        for (NotificationContext context : contexts) {
            // Persist notification
            Notification notification = notificationCommandService.createNotification(context);

            // Send notification if created
            if (notification != null) {
                notificationCommandService.sendNotification(notification);
            }
        }
    }

    @EventListener(RequestApprovedEvent.class)
    @Async
    void handleRequestApprovedEvent(RequestApprovedEvent event) {
        // Map domain event to notification context
        NotificationContext context = createContextFromEvent(event);
        if (context == null) {
            return;
        }

        // Persist notification
        Notification notification = notificationCommandService.createNotification(context);

        // Send notification if created
        if (notification != null) {
            notificationCommandService.sendNotification(notification);
        }
    }

    @EventListener(RequestRejectedEvent.class)
    @Async
    void handleRequestRejectedEvent(RequestRejectedEvent event) {
        // Map domain event to notification context
        NotificationContext context = createContextFromEvent(event);
        if (context == null) {
            return;
        }

        // Persist notification
        Notification notification = notificationCommandService.createNotification(context);

        // Send notification if created
        if (notification != null) {
            notificationCommandService.sendNotification(notification);
        }
    }

    private NotificationContext createContextFromEvent(RequestApprovedEvent event) {
        Request request = (Request) event.getRequest();
        String employeeId = safeUserId(request.getEmployee());
        if (employeeId == null) {
            return null;
        }

        return NotificationContext.builder()
                .recipientId(employeeId)
                .userRole(UserRole.EMPLOYEE.name())
                .type(NotificationType.REQUEST_APPROVED)
                .referenceType(NotificationReferenceType.REQUEST)
                .referenceId(request.getRequestId())
                .templateData(Map.of(
                    "employeeName", safeFullName(request.getEmployee()),
                    "requestType", request.getRequestType().name(),
                    "approverName", safeFullName(request.getApprover()),
                    "processedAt", request.getProcessedAt() == null ? "" : request.getProcessedAt().format(DATE_TIME_FORMATTER)
                ))
                .build();
    }
    private NotificationContext createContextFromEvent(RequestRejectedEvent event) {
        Request request = (Request) event.getRequest();
        String employeeId = safeUserId(request.getEmployee());
        if (employeeId == null) {
            return null;
        }

        return NotificationContext.builder()
                .recipientId(employeeId)
                .userRole(UserRole.EMPLOYEE.name())
                .type(NotificationType.REQUEST_REJECTED)
                .referenceType(NotificationReferenceType.REQUEST)
                .referenceId(request.getRequestId())
                .templateData(Map.of(
                    "employeeName", safeFullName(request.getEmployee()),
                    "requestType", request.getRequestType().name(),
                    "approverName", safeFullName(request.getApprover()),
                    "processedAt", request.getProcessedAt() == null ? "" : request.getProcessedAt().format(DATE_TIME_FORMATTER),
                    "rejectionReason", safeString(request.getRejectReason())
                ))
                .build();
    }
    private List<NotificationContext> createContextFromEvent(RequestCreatedEvent event) {
        Request request = (Request) event.getRequest();
        List<NotificationContext> contexts = new java.util.ArrayList<>();

        String employeeId = safeUserId(request.getEmployee());
        if (employeeId != null) {
            NotificationContext employeeContext = NotificationContext.builder()
                    .recipientId(employeeId)
                    .userRole(UserRole.EMPLOYEE.name())
                    .type(NotificationType.REQUEST_CREATED)
                    .referenceType(NotificationReferenceType.REQUEST)
                    .referenceId(request.getRequestId())
                    .templateData(Map.of(
                        "employeeName", safeFullName(request.getEmployee()),
                        "approverName", safeFullName(request.getApprover()),
                        "requestType", request.getRequestType().name(),
                        "createdAt", request.getCreatedAt() == null ? "" : request.getCreatedAt().format(DATE_TIME_FORMATTER)
                    ))
                    .build();

            contexts.add(employeeContext);
        }

        String approverId = safeUserId(request.getApprover());
        if (approverId != null) {
            NotificationContext managerContext = NotificationContext.builder()
                    .recipientId(approverId)
                    .userRole(UserRole.MANAGER.name())
                    .type(NotificationType.REQUEST_CREATED)
                    .referenceType(NotificationReferenceType.REQUEST)
                    .referenceId(request.getRequestId())
                    .templateData(Map.of(
                        "employeeName", safeFullName(request.getEmployee()),
                        "approverName", safeFullName(request.getApprover()),
                        "requestType", request.getRequestType().name(),
                        "createdAt", request.getCreatedAt() == null ? "" : request.getCreatedAt().format(DATE_TIME_FORMATTER)
                    ))
                    .build();

            contexts.add(managerContext);
        }

        return contexts;
    }

    private String safeFullName(User user) {
        if (user == null) return "";
        String name = user.getFullName();
        return name == null ? "" : name;
    }

    private String safeUserId(User user) {
        if (user == null) return null;
        String id = user.getUserId();
        return (id == null || id.isBlank()) ? null : id;
    }

    private String safeString(String s) {
        return s == null ? "" : s;
    }

}
