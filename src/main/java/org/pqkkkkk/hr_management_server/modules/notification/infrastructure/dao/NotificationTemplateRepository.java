package org.pqkkkkk.hr_management_server.modules.notification.infrastructure.dao;

import java.util.List;
import java.util.Optional;

import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.NotificationTemplate;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, String> {
    @Query(value =  """
        SELECT nt FROM NotificationTemplate nt
        WHERE nt.notificationType = :type
        AND (nt.userRole IS NULL OR nt.userRole = :userRole)
        AND nt.isActive = :isActive
            """)
    Optional<NotificationTemplate> findByNotificationTypeAndUserRoleAndIsActive(NotificationType type, String userRole, Boolean isActive);
    List<NotificationTemplate> findByNotificationTypeAndIsActive(NotificationType type, Boolean isActive);
}
