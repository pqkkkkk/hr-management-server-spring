package org.pqkkkkk.hr_management_server.modules.notification.infrastructure.dao;

import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, String>,
                                               JpaSpecificationExecutor<Notification> {
    
    // Count unread notifications for a specific recipient
    Long countByRecipientIdAndIsRead(String recipientId, Boolean isRead);
    
    // Mark all notifications as read for a specific recipient
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientId = :recipientId AND n.isRead = false")
    Integer markAllAsReadByRecipientId(@Param("recipientId") String recipientId);
}
