package org.pqkkkkk.hr_management_server.modules.notification.controller.event_listener;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.NotificationContext;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationReferenceType;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationType;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Notification;
import org.pqkkkkk.hr_management_server.modules.notification.domain.service.NotificationCommandService;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
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
        NotificationContext context = createContextFromEvent(event);

        // Persist notification
        Notification notification = notificationCommandService.createNotification(context);

        // Send notification
        notificationCommandService.sendNotification(notification);
    }

    @EventListener(RequestApprovedEvent.class)
    @Async
    void handleRequestApprovedEvent(RequestApprovedEvent event) {
        // Map domain event to notification context
        NotificationContext context = createContextFromEvent(event);

        // Persist notification
         Notification notification = notificationCommandService.createNotification(context);

        // Send notification
        notificationCommandService.sendNotification(notification);
    }

    @EventListener(RequestRejectedEvent.class)
    @Async
    void handleRequestRejectedEvent(RequestRejectedEvent event) {
        // Map domain event to notification context
        NotificationContext context = createContextFromEvent(event);

        // Persist notification
        Notification notification = notificationCommandService.createNotification(context);

        // Send notification
        notificationCommandService.sendNotification(notification);
    }

    private NotificationContext createContextFromEvent(RequestApprovedEvent event) {
        Request request = (Request) event.getRequest();

        return NotificationContext.builder()
                .recipientId(request.getEmployee().getUserId())
                .type(NotificationType.REQUEST_APPROVED)
                .referenceType(NotificationReferenceType.REQUEST)
                .referenceId(request.getRequestId())
                .templateData(Map.of(
                    "employeeName", request.getEmployee().getFullName(),
                    "requestType", request.getRequestType().name(),
                    "approverName", request.getApprover().getFullName(),
                    "processedAt", request.getProcessedAt() == null ? "" : request.getProcessedAt().format(DATE_TIME_FORMATTER)
                ))
                .build();
    }
    private NotificationContext createContextFromEvent(RequestRejectedEvent event) {
        Request request = (Request) event.getRequest();

        return NotificationContext.builder()
                .recipientId(request.getEmployee().getUserId())
                .type(NotificationType.REQUEST_REJECTED)
                .referenceType(NotificationReferenceType.REQUEST)
                .referenceId(request.getRequestId())
                .templateData(Map.of(
                    "employeeName", request.getEmployee().getFullName(),
                    "requestType", request.getRequestType().name(),
                    "approverName", request.getApprover().getFullName(),
                    "processedAt", request.getProcessedAt() == null ? "" : request.getProcessedAt().format(DATE_TIME_FORMATTER),
                    "rejectionReason", request.getRejectReason()
                ))
                .build();
    }
    private NotificationContext createContextFromEvent(RequestCreatedEvent event) {
        Request request = (Request) event.getRequest();

        return NotificationContext.builder()
                .recipientId(request.getEmployee().getUserId())
                .type(NotificationType.REQUEST_CREATED)
                .referenceType(NotificationReferenceType.REQUEST)
                .referenceId(request.getRequestId())
                .templateData(Map.of(
                    "employeeName", request.getEmployee().getFullName(),
                    "approverName", request.getApprover().getFullName(),
                    "requestType", request.getRequestType().name(),
                    "createdAt", request.getCreatedAt() == null ? "" : request.getCreatedAt().format(DATE_TIME_FORMATTER)
                ))
                .build();
    }

}
