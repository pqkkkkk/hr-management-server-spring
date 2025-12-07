package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import org.pqkkkkk.hr_management_server.modules.request.domain.dao.RequestDao;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.filter.FilterCriteria.RequestFilter;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestQueryService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequestQueryServiceImpl implements RequestQueryService {

    private final RequestDao requestDao;

    public RequestQueryServiceImpl(RequestDao requestDao) {
        this.requestDao = requestDao;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Request> getRequests(RequestFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter cannot be null");
        }
        
        return requestDao.getRequests(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public Request getRequestById(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Request ID cannot be null or empty");
        }
        
        Request request = requestDao.getRequestById(requestId);
        
        if (request == null) {
            throw new IllegalArgumentException("Request not found with ID: " + requestId);
        }
        
        return request;
    }
}
