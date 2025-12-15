package org.pqkkkkk.hr_management_server.modules.notification.infrastructure;

import org.pqkkkkk.hr_management_server.modules.notification.domain.dao.NotificationTemplateDao;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationTemplateMockDao implements NotificationTemplateDao {
    @Override
    public String getTemplateByType(String type) {
        switch (type) {
            case "REQUEST_CREATED":
                return "Yêu cầu {requestType} của bạn đã được tạo thành công vào lúc {createdAt}.";
            case "REQUEST_APPROVED":
                return "Yêu cầu {requestType} của bạn đã được phê duyệt bởi quản lý {approverName} vào lúc {processedAt}.";
            case "REQUEST_REJECTED":
                return "Yêu cầu {requestType} của bạn đã bị từ chối bởi quản lý {approverName} vào lúc {processedAt}. Lý do: {rejectionReason}.";
            default:
                return "This notification is not valid.";
        }
    }

}
