package org.pqkkkkk.hr_management_server.modules.notification.infrastructure.dao;

import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, String> {

}
