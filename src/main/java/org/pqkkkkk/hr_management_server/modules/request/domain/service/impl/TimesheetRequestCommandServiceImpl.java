package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileQueryService;
import org.pqkkkkk.hr_management_server.modules.request.domain.dao.RequestDao;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.event.RequestCreatedEvent;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestCommandService;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestDelegationService;
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
    
    public TimesheetRequestCommandServiceImpl(RequestDao requestDao, ProfileQueryService profileQueryService,
        ApplicationEventPublisher eventPublisher, RequestDelegationService delegationService) {
        this.requestDao = requestDao;
        this.profileQueryService = profileQueryService;
        this.eventPublisher = eventPublisher;
        this.delegationService = delegationService;
    }
    private void validateRequestInfo(Request request) {
        // 1. Validate employee
        if (request.getEmployee() == null || request.getEmployee().getUserId() == null ) {
            throw new IllegalArgumentException("Employee does not exist");
        }
        
        // 2. Validate work date (not more than 7 days in the past)
        LocalDate workDate = request.getAdditionalTimesheetInfo() != null 
            ? request.getAdditionalTimesheetInfo().getTargetDate() 
            : null;

        if (workDate == null || workDate.isBefore(LocalDate.now().minusDays(7)) 
            || workDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Work date is invalid");
        }

        // 3. Validate requested times
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

        if (checkIn == null && checkOut == null) {
            throw new IllegalArgumentException("At least one of check-in or check-out must be provided");
        }

        if (checkIn != null && checkOut != null && !checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException("Check-in must be before check-out");
        }

        LocalTime start = LocalTime.of(6, 0);
        LocalTime end = LocalTime.of(22, 0);
        if ((checkIn != null && (checkIn.isBefore(start) || checkIn.isAfter(end))) ||
            (checkOut != null && (checkOut.isBefore(start) || checkOut.isAfter(end)))) {
            throw new IllegalArgumentException("Requested times must be within working hours (6:00 - 22:00)");
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

        Request createdRequest =requestDao.createRequest(request);

        eventPublisher.publishEvent(new RequestCreatedEvent(this, createdRequest));

        return createdRequest;
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
    @Override
    public Request delegateRequest(String requestId, String newProcessorId) {
        return delegationService.delegateRequest(requestId, newProcessorId);
    }

}


