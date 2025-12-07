package org.pqkkkkk.hr_management_server.modules.request.domain.service;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;

public interface RequestCommandService {
    public Request createLeaveRequest(Request request);
    public Request approveRequest(String requestId, String approverId);
    public Request rejectRequest(String requestId, String approverId, String rejectionReason);
}
