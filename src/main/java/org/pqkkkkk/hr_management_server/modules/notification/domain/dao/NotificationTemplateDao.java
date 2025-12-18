package org.pqkkkkk.hr_management_server.modules.notification.domain.dao;

import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.NotificationTemplate;

public interface NotificationTemplateDao {
    public NotificationTemplate getTemplateByType(String type);

    /**
     * Get message template for a given notification type and user role.
     * Returns null if not found.
     */
    public NotificationTemplate getTemplateByTypeAndUserRole(String type, String userRole);
}
