package org.pqkkkkk.hr_management_server.modules.notification.infrastructure.dao;

import org.pqkkkkk.hr_management_server.modules.notification.domain.dao.NotificationTemplateDao;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.NotificationTemplate;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationType;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationTemplateJpaDao implements NotificationTemplateDao {
    private final NotificationTemplateRepository repository;

    public NotificationTemplateJpaDao(NotificationTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    public NotificationTemplate getTemplateByType(String type) {
        throw new UnsupportedOperationException("Use getTemplateByTypeAndUserRole instead.");
    }

    @Override
    public NotificationTemplate getTemplateByTypeAndUserRole(String type, String userRole) {
        return repository.findByNotificationTypeAndUserRoleAndIsActive(NotificationType.valueOf(type), userRole, true)
                .orElse(null);
    }
}
