package org.pqkkkkk.hr_management_server.modules.notification.domain.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationReferenceType;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "notification_table")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class Notification {
    @Id
    @Column(name = "notification_id")
    @UuidGenerator
    String notificationId;

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = "message", nullable = false)
    String message;

    @Column(name = "notification_type", nullable = false)
    @Enumerated(EnumType.STRING)
    NotificationType type;

    @Column(name = "reference_type", nullable = false)
    @Enumerated(EnumType.STRING)
    NotificationReferenceType referenceType;

    @Column(name = "reference_id")
    String referenceId;

    @Column(name = "is_read", nullable = false)
    Boolean isRead;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    LocalDateTime createdAt;

    @Column(name = "recipient_id", nullable = false)
    String recipientId;
}
