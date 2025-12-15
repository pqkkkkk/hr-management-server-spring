package org.pqkkkkk.hr_management_server.modules.notification.controller.http;

import org.pqkkkkk.hr_management_server.modules.notification.controller.http.dto.DTO.NotificationDTO;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Notification;
import org.pqkkkkk.hr_management_server.modules.notification.domain.filter.FilterCriteria.NotificationFilter;
import org.pqkkkkk.hr_management_server.modules.notification.domain.service.NotificationCommandService;
import org.pqkkkkk.hr_management_server.modules.notification.domain.service.NotificationQueryService;
import org.pqkkkkk.hr_management_server.modules.notification.domain.service.delivery.SseNotificationDelivery;
import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    private final NotificationCommandService notificationCommandService;
    private final NotificationQueryService notificationQueryService;
    private final SseNotificationDelivery sseNotificationDelivery;

    public NotificationApi(
        NotificationCommandService notificationCommandService,
        NotificationQueryService notificationQueryService,
        SseNotificationDelivery sseNotificationDelivery
    ) {
        this.notificationCommandService = notificationCommandService;
        this.notificationQueryService = notificationQueryService;
        this.sseNotificationDelivery = sseNotificationDelivery;
    }

    /**
     * SSE endpoint for real-time notification streaming
     * GET /api/v1/notifications/stream?userId={userId}
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(@RequestParam String userId) {
        log.info("User {} connecting to SSE stream", userId);
        return sseNotificationDelivery.subscribe(userId);
    }

    /**
     * Get paginated list of notifications with filtering
     * GET /api/v1/notifications?recipientId={userId}&isRead={true/false}&currentPage=0&pageSize=10
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getNotifications(
        @ModelAttribute NotificationFilter filter
    ) {
        Page<Notification> notifications = notificationQueryService.getNotifications(filter);
        Page<NotificationDTO> notificationDTOs = notifications.map(NotificationDTO::fromEntity);
        
        return ResponseEntity.ok(new ApiResponse<>(
            notificationDTOs,
            true,
            200,
            "Notifications retrieved successfully",
            null
        ));
    }

    /**
     * Get count of unread notifications for a user
     * GET /api/v1/notifications/unread-count?userId={userId}
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@RequestParam String userId) {
        Long count = notificationQueryService.getUnreadCount(userId);
        
        return ResponseEntity.ok(new ApiResponse<>(
            count,
            true,
            200,
            "Unread notification count retrieved successfully",
            null
        ));
    }

    /**
     * Mark a single notification as read
     * PATCH /api/v1/notifications/read?notificationId={notificationId}
     */
    @PatchMapping("/read")
    public ResponseEntity<ApiResponse<Void>> markNotificationAsRead(@RequestParam String notificationId) {
        notificationCommandService.markAsRead(notificationId);
        
        return ResponseEntity.ok(new ApiResponse<>(
            null,
            true,
            200,
            "Notification marked as read",
            null
        ));
    }

    /**
     * Mark all notifications as read for a user
     * PATCH /api/v1/notifications/mark-all-read?userId={userId}
     */
    @PatchMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@RequestParam String userId) {
        notificationCommandService.markAllAsRead(userId);
        
        return ResponseEntity.ok(new ApiResponse<>(
            null,
            true,
            200,
            "All notifications marked as read",
            null
        ));
    }
}
