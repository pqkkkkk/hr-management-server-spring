package org.pqkkkkk.hr_management_server.modules.notification.controller.http;

import org.pqkkkkk.hr_management_server.modules.notification.domain.service.NotificationService;
import org.pqkkkkk.hr_management_server.modules.notification.domain.service.delivery.SseNotificationDelivery;
import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/notifications")
@Slf4j
public class NotificationApi {
    private final NotificationService notificationService;
    private SseNotificationDelivery sseNotificationDelivery;

    public NotificationApi(NotificationService notificationService, SseNotificationDelivery sseNotificationDelivery) {
        this.notificationService = notificationService;
        this.sseNotificationDelivery = sseNotificationDelivery;
    }

    // SSE endpoint
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(@RequestParam String userId) {
        log.info("User {} connecting to SSE stream", userId);
        return sseNotificationDelivery.subscribe(userId);
    }

    @PatchMapping("/read")
    public ResponseEntity<ApiResponse<Void>> markNotificationAsRead(@RequestParam String notificationId) {
        notificationService.markAsRead(notificationId);
        
        return ResponseEntity.ok(new ApiResponse<>(
            null,
            true,
            200,
            "Notification marked as read",
            null
        ));
    }

}
