package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileQueryService;
import org.pqkkkkk.hr_management_server.modules.request.domain.dao.RequestDao;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.AdditionalTimesheetInfo;
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
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;

import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class TimesheetRequestCommandServiceImpl implements RequestCommandService {
    private final RequestDao requestDao;
    private final ProfileQueryService profileQueryService;
    private final ApplicationEventPublisher eventPublisher;
    private final RequestDelegationService delegationService;
    private final RequestValidationService requestValidationService;
    private final TimeSheetCommandService timeSheetCommandService;

    public TimesheetRequestCommandServiceImpl(
            RequestDao requestDao,
            ProfileQueryService profileQueryService,
            ApplicationEventPublisher eventPublisher,
            RequestDelegationService delegationService,
            RequestValidationService requestValidationService,
            TimeSheetCommandService timeSheetCommandService) {
        this.requestDao = requestDao;
        this.profileQueryService = profileQueryService;
        this.eventPublisher = eventPublisher;
        this.delegationService = delegationService;
        this.requestValidationService = requestValidationService;
        this.timeSheetCommandService = timeSheetCommandService;
    }

    private void validateRequestInfo(Request request) {
        // 1. Validate employee
        if (request.getEmployee() == null || request.getEmployee().getUserId() == null) {
            throw new IllegalArgumentException("Employee does not exist");
        }

        // 2. Validate additional timesheet info
        if (request.getAdditionalTimesheetInfo() == null) {
            throw new IllegalArgumentException("Additional timesheet info is required");
        }

        // 3. Validate target date
        LocalDate targetDate = request.getAdditionalTimesheetInfo() != null
                ? request.getAdditionalTimesheetInfo().getTargetDate()
                : null;

        if (targetDate == null) {
            throw new IllegalArgumentException("Target date is required");
        }

        // 4. Validate requested times
        LocalTime checkIn = request.getAdditionalTimesheetInfo() != null
                ? (request.getAdditionalTimesheetInfo().getDesiredCheckInTime() != null
                        ? request.getAdditionalTimesheetInfo().getDesiredCheckInTime().toLocalTime()
                        : null)
                : null;

        LocalTime checkOut = request.getAdditionalTimesheetInfo() != null
                ? (request.getAdditionalTimesheetInfo().getDesiredCheckOutTime() != null
                        ? request.getAdditionalTimesheetInfo().getDesiredCheckOutTime().toLocalTime()
                        : null)
                : null;

        if (checkIn != null && checkOut != null && !checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException("Check-in must be before check-out");
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

        request.setRequestType(RequestType.TIMESHEET);
        request.setStatus((RequestStatus.PENDING));
        request.setCreatedAt(LocalDateTime.now());

        Request createdRequest = requestDao.createRequest(request);

        eventPublisher.publishEvent(new RequestCreatedEvent(this, createdRequest));

        return createdRequest;
    }

    @Override
    @Transactional
    public Request approveRequest(String requestId, String approverId) {
        // Step 1: Validate request exists and is in valid state
        Request req = requestValidationService.checkRequestIsValid(requestId);

        // Step 2: Verify approver has permission
        requestValidationService.checkApproverPermissions(approverId, req);

        // Step 3: Update request status and timestamp
        req.setStatus(RequestStatus.APPROVED);
        req.setProcessedAt(LocalDateTime.now());

        // Step 4: Update timesheet with desired times
        AdditionalTimesheetInfo timesheetInfo = req.getAdditionalTimesheetInfo();
        if (timesheetInfo != null) {
            timeSheetCommandService.handleTimesheetUpdateApproval(
                    req.getEmployee().getUserId(),
                    timesheetInfo.getTargetDate(),
                    timesheetInfo.getDesiredCheckInTime(),
                    timesheetInfo.getDesiredCheckOutTime(),
                    timesheetInfo.getDesiredMorningStatus(),
                    timesheetInfo.getDesiredAfternoonStatus(),
                    timesheetInfo.getDesiredMorningWfh(),
                    timesheetInfo.getDesiredAfternoonWfh());
        }

        // Step 5: Publish event for further processing (e.g., notifications)
        eventPublisher.publishEvent(new RequestApprovedEvent(this, req));

        return req;
    }

    @Override
    @Transactional
    public Request rejectRequest(String requestId, String approverId, String rejectionReason) {
        // Step 1: Validate request exists and is in valid state
        Request req = requestValidationService.checkRequestIsValid(requestId);

        // Step 2: Verify approver has permission
        requestValidationService.checkApproverPermissions(approverId, req);

        // Step 3: Validate rejection reason
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required when rejecting a request.");
        }

        // Step 4: Update request status, reason and timestamp
        req.setStatus(RequestStatus.REJECTED);
        req.setRejectReason(rejectionReason);
        req.setProcessedAt(LocalDateTime.now());

        // Step 5: Publish event for further processing (e.g., notifications)
        eventPublisher.publishEvent(new RequestRejectedEvent(this, req));

        return req;
    }

    @Override
    public Request delegateRequest(String requestId, String newProcessorId) {
        return delegationService.delegateRequest(requestId, newProcessorId);
    }
}
