package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.dao.RequestDao;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestValidationService;
import org.springframework.stereotype.Service;

@Service
public class RequestValidationServiceImpl implements RequestValidationService {
    private final RequestDao requestDao;

    public RequestValidationServiceImpl(RequestDao requestDao) {
        this.requestDao = requestDao;
    }
    public Request checkRequestIsValid(String requestId) {
        Request existingRequest = requestDao.getRequestById(requestId);

        if (existingRequest == null) {
            throw new IllegalArgumentException("Request does not exist.");
        }

        if(existingRequest.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not in a valid state for this operation.");
        }

        return existingRequest;
    }

    public void checkApproverPermissions(String approverId, Request request) {
        String actualApproverId = request.getApprover() == null ? null : request.getApprover().getUserId();
        String actualProcessorId = request.getProcessor() == null ? null : request.getProcessor().getUserId();

        if(!approverId.equals(actualApproverId) && !approverId.equals(actualProcessorId)) {
            throw new SecurityException("Approver does not have permission to perform this action.");
        }
    }
}
