package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileQueryService;
import org.pqkkkkk.hr_management_server.modules.request.domain.dao.RequestDao;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.event.RequestApprovedEvent;
import org.pqkkkkk.hr_management_server.modules.request.domain.event.RequestCreatedEvent;
import org.pqkkkkk.hr_management_server.modules.request.domain.event.RequestRejectedEvent;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestCommandService;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestDelegationService;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestValidationService;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.service.TimeSheetCommandService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class CheckOutRequestCommandService implements RequestCommandService {
    private final RequestDao requestDao;
    private final ProfileQueryService profileQueryService;
    private final ApplicationEventPublisher eventPublisher;
    private final RequestValidationService requestValidationService;
    private final TimeSheetCommandService timeSheetCommandService;
    private final RequestDelegationService delegationService;
    private final static LocalTime CHECK_OUT_EARLY_TIME = LocalTime.of(17, 0); // 5:00 PM

    public CheckOutRequestCommandService(RequestDao requestDao, ProfileQueryService profileQueryService,
        ApplicationEventPublisher eventPublisher, RequestDelegationService delegationService,
        RequestValidationService requestValidationService, TimeSheetCommandService timeSheetCommandService
    ) {
        this.requestDao = requestDao;
        this.profileQueryService = profileQueryService;
        this.eventPublisher = eventPublisher;
        this.delegationService = delegationService;
        this.requestValidationService = requestValidationService;
        this.timeSheetCommandService = timeSheetCommandService;
    }
    private void validateRequestInfo(Request request) {
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

        LocalDateTime desiredCheckOutTime = request.getAdditionalCheckOutInfo().getDesiredCheckOutTime();
        // Validate checkOutTime
        if (desiredCheckOutTime == null) {
            throw new IllegalArgumentException("Desired check-out time is required.");
        }
        if(desiredCheckOutTime.toLocalTime().isBefore(CHECK_OUT_EARLY_TIME) && request.getUserReason()== null){
            throw new IllegalArgumentException("A reason must be provided for check-out requests before " + CHECK_OUT_EARLY_TIME);
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

        // Set requestType (CHECK OUT ) and status(PENDING)
        request.setRequestType(RequestType.CHECK_OUT);
        request.setStatus((RequestStatus.PENDING));
        request.setCreatedAt(LocalDateTime.now());

        // Link back request in additionalCheckOutInfo
        request.getAdditionalCheckOutInfo().setRequest(request);

        Request createdRequest = requestDao.createRequest(request);

        eventPublisher.publishEvent(new RequestCreatedEvent(this, createdRequest));

        return createdRequest;
    }

    @Override
    @Transactional
    public Request approveRequest(String requestId, String approverId) {
        Request req = requestValidationService.checkRequestIsValid(requestId);

        requestValidationService.checkApproverPermissions(approverId, req);

        req.setStatus(RequestStatus.APPROVED);
        req.setProcessedAt(LocalDateTime.now());

        timeSheetCommandService.handleCheckOutApproval(req.getEmployee().getUserId(), req.getAdditionalCheckOutInfo().getDesiredCheckOutTime());

        eventPublisher.publishEvent(new RequestApprovedEvent(this, req));
        
        return req;
    }

    @Override
    @Transactional
    public Request rejectRequest(String requestId, String approverId, String rejectionReason) {
        Request req = requestValidationService.checkRequestIsValid(requestId);

        requestValidationService.checkApproverPermissions(approverId, req);

        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required when rejecting a request.");
        }

        req.setStatus(RequestStatus.REJECTED);
        req.setProcessedAt(LocalDateTime.now());
        req.setRejectReason(rejectionReason);

        eventPublisher.publishEvent(new RequestRejectedEvent(this, req));
        
        return req;
    }

    @Override
    public Request delegateRequest(String requestId, String newProcessorId) {
        return delegationService.delegateRequest(requestId, newProcessorId);
    }

}
