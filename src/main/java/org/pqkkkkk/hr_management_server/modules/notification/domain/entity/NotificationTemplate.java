package org.pqkkkkk.hr_management_server.modules.notification.domain.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "notification_template_table", uniqueConstraints = @UniqueConstraint(columnNames = {"notification_type", "user_role"}))
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class NotificationTemplate {
    @Id
    @Column(name = "template_id")
    @UuidGenerator
    String templateId;

    @Column(name = "notification_type", nullable = false)
    @Enumerated(EnumType.STRING)
    NotificationType notificationType;

    @Column(name = "user_role", nullable = false)
    String userRole;

    @Column(name = "title_template")
    String titleTemplate;

    @Column(name = "message_template", nullable = false, columnDefinition = "TEXT")
    String messageTemplate;

    @Column(name = "is_active", nullable = false)
    Boolean isActive;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    LocalDateTime updatedAt;
}
