package org.pqkkkkk.hr_management_server.modules.notification.domain.entity;

import java.util.Map;

import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationReferenceType;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationType;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class NotificationContext {
    String recipientId;
    NotificationType type;
    NotificationReferenceType referenceType;
    String referenceId;
    Map<String, Object> templateData;
}
