-- Seed default notification templates for different user roles
INSERT INTO notification_template_table (template_id, notification_type, user_role, title_template, message_template, is_active)
VALUES
('tmpl-0001-0000-0000-000000000001', 'REQUEST_CREATED', 'EMPLOYEE', 'Yêu cầu mới', 'Yêu cầu {requestType} của bạn đã được tạo thành công vào lúc {createdAt}.', true),
('tmpl-0001-0000-0000-000000000002', 'REQUEST_CREATED', 'MANAGER', 'Yêu cầu mới từ nhân viên', 'Nhân viên {employeeName} đã gửi yêu cầu {requestType} vào lúc {createdAt}.', true),
('tmpl-0001-0000-0000-000000000003', 'REQUEST_CREATED', 'HR', 'Yêu cầu mới từ nhân viên', 'Nhân viên {employeeName} đã gửi yêu cầu {requestType} vào lúc {createdAt}.', true),
('tmpl-0001-0000-0000-000000000004', 'REQUEST_CREATED', 'ADMIN', 'Yêu cầu mới từ nhân viên', 'Nhân viên {employeeName} đã gửi yêu cầu {requestType} vào lúc {createdAt}.', true),
('tmpl-0001-0000-0000-000000000005', 'REQUEST_APPROVED', 'EMPLOYEE', 'Yêu cầu đã được phê duyệt', 'Yêu cầu {requestType} của bạn đã được phê duyệt bởi quản lý {approverName} vào lúc {processedAt}.', true),
('tmpl-0001-0000-0000-000000000007', 'REQUEST_REJECTED', 'EMPLOYEE', 'Yêu cầu bị từ chối', 'Yêu cầu {requestType} của bạn đã bị từ chối bởi {approverName} vào lúc {processedAt}. Lý do: {rejectionReason}.', true);