package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileQueryService;
import org.pqkkkkk.hr_management_server.modules.request.domain.dao.RequestDao;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestCommandService;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class CheckInRequestCommandService implements RequestCommandService {
    private final RequestDao requestDao;
    private final ProfileQueryService profileQueryService;
    private static final LocalTime CHECK_IN_DEADLINE = LocalTime.of(8, 0); // 8:00 AM

    public CheckInRequestCommandService(RequestDao requestDao, ProfileQueryService profileQueryService) {
        this.requestDao = requestDao;
        this.profileQueryService = profileQueryService;
    }
    private void validateRequestInfo(Request request) {
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

        LocalDateTime desiredCheckInTime = request.getAdditionalCheckInInfo().getDesiredCheckInTime();
        // Validate desiredCheckInTime
        if (desiredCheckInTime == null) {
            throw new IllegalArgumentException("Desired check-in time is required.");
        }
        if(desiredCheckInTime.toLocalTime().isAfter(CHECK_IN_DEADLINE) && request.getUserReason()== null){
            throw new IllegalArgumentException("A reason must be provided for check-in requests after " + CHECK_IN_DEADLINE);
        }

    }
    @Override
    @Transactional
    public Request createRequest(Request request) {
        validateRequestInfo(request);

        User employee = profileQueryService.getProfileById(request.getEmployee().getUserId());
        request.setEmployee(employee);
        request.setProcessor(employee.getManager());
        request.setApprover(employee.getManager());

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
