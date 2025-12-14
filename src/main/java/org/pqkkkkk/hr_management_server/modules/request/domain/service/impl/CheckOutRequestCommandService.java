package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import java.time.LocalDate;

import org.pqkkkkk.hr_management_server.modules.request.domain.dao.RequestDao;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestCommandService;
import org.springframework.stereotype.Service;

@Service
public class CheckOutRequestCommandService implements RequestCommandService {
    private final RequestDao requestDao;

    public CheckOutRequestCommandService(RequestDao requestDao) {
        this.requestDao = requestDao;
    }
    @Override
    public Request createRequest(Request request) {
        // Validate request 
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null.");
        }
    
        // Validate employee
        if (request.getEmployee() == null || request.getEmployee().getUserId() == null) {
            throw new IllegalArgumentException("Employee information is required for check-out request creation.");
        }

        // Validate additionalCheckOutInfo
        if (request.getAdditionalCheckOutInfo() == null) {
            throw new IllegalArgumentException("Additional check-out information is required for check-out request creation.");
        }
    
        // Validate desiredCheckOutTime and currentCheckOutTime
        if (request.getAdditionalCheckOutInfo().getDesiredCheckOutTime() == null) {
            throw new IllegalArgumentException("Desired check-out time is required.");
        }
    
        if (request.getAdditionalCheckOutInfo().getCurrentCheckOutTime() == null) {
            throw new IllegalArgumentException("Current check-out time is required.");
        }

        // Check for duplicate check-out request on the same date
        LocalDate checkOutDate = request.getAdditionalCheckOutInfo().getDesiredCheckOutTime().toLocalDate();
        boolean checkOutExists = requestDao.existsByEmployeeAndDateAndType(request.getEmployee().getUserId(), checkOutDate, RequestType.CHECK_OUT);

        if (checkOutExists) {
            throw new IllegalArgumentException("Duplicate check-out request for date: " + checkOutDate);
        }

        // Check exists check in request for the same date
        boolean checkInExists = requestDao.existsByEmployeeAndDateAndType(request.getEmployee().getUserId(), checkOutDate, RequestType.CHECK_IN);

        if (!checkInExists) {
            throw new IllegalArgumentException("No corresponding check-in request found for date: " + checkOutDate);
        }

        // Set requestType (CHECK OUT ) and status(PENDING)
        request.setRequestType(RequestType.CHECK_OUT);

        request.setStatus((RequestStatus.PENDING));

        // Link back request in additionalCheckOutInfo
        request.getAdditionalCheckOutInfo().setRequest(request);

        return requestDao.createRequest(request);
    }

    @Override
    public Request approveRequest(String requestId, String approverId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'approveRequest'");
    }

    @Override
    public Request rejectRequest(String requestId, String approverId, String rejectionReason) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rejectRequest'");
    }

}
