package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import java.time.LocalDate;

import org.pqkkkkk.hr_management_server.modules.request.domain.dao.RequestDao;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestCommandService;
import org.springframework.stereotype.Service;

@Service
public class CheckInRequestCommandService implements RequestCommandService {
    private final RequestDao requestDao;

    public CheckInRequestCommandService(RequestDao requestDao) {
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
                throw new IllegalArgumentException("Employee information is required for check-in request creation.");
            }

            // Validate additionalCheckInInfo
            if (request.getAdditionalCheckInInfo() == null) {
                throw new IllegalArgumentException("Additional check-in information is required for check-in request creation.");
            }
        
            // Validate desiredCheckInTime and currentCheckInTime
            if (request.getAdditionalCheckInInfo().getDesiredCheckInTime() == null) {
                throw new IllegalArgumentException("Desired check-in time is required.");
            }
        
            if (request.getAdditionalCheckInInfo().getCurrentCheckInTime() == null) {
                throw new IllegalArgumentException("Current check-in time is required.");
            }
            
            // Check for duplicate check-in request on the same date
            LocalDate checkInDate = request.getAdditionalCheckInInfo().getDesiredCheckInTime().toLocalDate();
            boolean checkInExists = requestDao.existsByEmployeeAndDateAndType(request.getEmployee().getUserId(), checkInDate, RequestType.CHECK_IN);

            if (checkInExists) {
                throw new IllegalArgumentException("Duplicate check-in request for date: " + checkInDate);
            }
    
            // Set requestType (CHECK IN ) and status(PENDING)
            request.setRequestType(RequestType.CHECK_IN);

            request.setStatus((RequestStatus.PENDING));

            // Link back request in additionalCheckInInfo
            request.getAdditionalCheckInInfo().setRequest(request);

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
