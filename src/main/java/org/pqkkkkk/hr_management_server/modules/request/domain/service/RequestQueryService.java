package org.pqkkkkk.hr_management_server.modules.request.domain.service;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.filter.FilterCriteria.RequestFilter;
import org.springframework.data.domain.Page;

public interface RequestQueryService {
    /**
     * Get requests with filtering and pagination
     * @param filter - contains all filter criteria including employeeId, departmentId, status, type, date range, pagination
     * @return Page of requests matching the filter
     */
    Page<Request> getRequests(RequestFilter filter);
    
    /**
     * Get request by ID
     * @param requestId - the ID of the request
     * @return Request entity or null if not found
     */
    Request getRequestById(String requestId);
}
