package org.pqkkkkk.hr_management_server.modules.notification.domain.service.delivery;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Notification;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationChannel;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SseNotificationDelivery implements NotificationDelivery {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    @Override
    public void deliver(Notification notification) {
        SseEmitter emitter = emitters.get(notification.getRecipientId());
        
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notification));
                    
                log.info("SSE notification sent to user: {}", notification.getRecipientId());
            } catch (IOException e) {
                log.error("Failed to send SSE: {}", e.getMessage());
                emitters.remove(notification.getRecipientId());
            }
        } else {
            log.debug("No SSE connection for user: {}", notification.getRecipientId());
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SSE;
    }

    public SseEmitter subscribe(String userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(userId, emitter);

        try {
            emitter.send(SseEmitter.event()
                .name("INIT")
                .data("SSE connection established."));
        } catch (IOException e) {
            log.error("Error sending INIT event: {}", e.getMessage());
        }
        
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((ex) -> emitters.remove(userId));

        return emitter;
    }

}
