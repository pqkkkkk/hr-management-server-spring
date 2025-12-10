package org.pqkkkkk.hr_management_server.modules.request.domain.dao;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.filter.FilterCriteria.RequestFilter;
import org.springframework.data.domain.Page;

public interface RequestDao {
    public Request createRequest(Request request);
    public Request updateRequest(Request request);
    public Request getRequestById(String requestId);
    public Page<Request> getRequests(RequestFilter filter);
    public boolean existsByEmployeeAndDateAndType(String employeeId, java.time.LocalDate date, RequestType type);
}
