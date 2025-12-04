package org.pqkkkkk.hr_management_server.modules.request.domain.service;

import org.pqkkkkk.hr_management_server.modules.request.domain.command.LeaveRequestFilterCommand;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.springframework.data.domain.Page;

public interface LeaveRequestQueryService {
    /**
     * Get paginated leave requests for a specific employee with filtering options
     * 
     * @param filterCommand Filter criteria including employeeId, status, requestType, dateRange
     * @return Page of Request entities sorted by createdAt DESC
     */
    Page<Request> getMyLeaveRequests(LeaveRequestFilterCommand filterCommand);
}
