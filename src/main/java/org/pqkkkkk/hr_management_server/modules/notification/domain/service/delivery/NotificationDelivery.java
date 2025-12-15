package org.pqkkkkk.hr_management_server.modules.notification.domain.service.delivery;

import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Notification;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationChannel;

public interface NotificationDelivery {
    public void deliver(Notification notification);
    public NotificationChannel getChannel();
}
