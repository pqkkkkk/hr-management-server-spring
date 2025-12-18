package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileQueryService;
import org.pqkkkkk.hr_management_server.modules.request.domain.dao.RequestDao;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.LeaveDate;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.ShiftType;
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

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Constants;

@Service
public class LeaveRequestCommandServiceImpl implements RequestCommandService {
    private final RequestDao requestDao;
    private final ProfileQueryService profileQueryService;
    private final RequestValidationService requestValidationService;
    private final TimeSheetCommandService timeSheetCommandService;
    private final ApplicationEventPublisher eventPublisher;
    private final RequestDelegationService delegationService;

    public LeaveRequestCommandServiceImpl(
            RequestDao requestDao, 
            ProfileQueryService profileQueryService,
            ApplicationEventPublisher eventPublisher,
            RequestDelegationService delegationService,
            RequestValidationService requestValidationService,
            TimeSheetCommandService timeSheetCommandService
    ) {
        this.requestDao = requestDao;
        this.profileQueryService = profileQueryService;
        this.eventPublisher = eventPublisher;
        this.requestValidationService = requestValidationService;                   
        this.delegationService = delegationService;
        this.timeSheetCommandService = timeSheetCommandService;
    }

    private void validateLeaveRequest(Request request) {
        // Validate request type
        if (request.getRequestType() != null && request.getRequestType() != RequestType.LEAVE) {
            throw new IllegalArgumentException("Invalid request type for leave request.");
        }
        
        // Validate title
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Request title is required for leave request creation.");
        }
        
        // Validate user reason
        if (request.getUserReason() == null || request.getUserReason().trim().isEmpty()) {
            throw new IllegalArgumentException("User reason is required for leave request creation.");
        }
        
        // Validate employee information
        if (request.getEmployee() == null || request.getEmployee().getUserId() == null) {
            throw new IllegalArgumentException("Employee information is required for leave request creation.");
        }
        
        // Validate additional leave info
        if (request.getAdditionalLeaveInfo() == null) {
            throw new IllegalArgumentException("Additional leave information is required for leave request creation.");
        }
        
        // Validate leave type
        if (request.getAdditionalLeaveInfo().getLeaveType() == null) {
            throw new IllegalArgumentException("Leave type is required for leave request creation.");
        }
        
        // Validate leave dates
        List<LeaveDate> leaveDates = request.getAdditionalLeaveInfo().getLeaveDates();
        if (leaveDates == null || leaveDates.isEmpty()) {
            throw new IllegalArgumentException("Leave dates cannot be empty for leave request creation.");
        }
        
        // Validate each leave date individually
        validateEachLeaveDate(leaveDates);
        
        // Validate advance notice requirement (must submit at least 2 working days before)
        validateAdvanceNotice(leaveDates);
    }
    
    private void validateEachLeaveDate(List<LeaveDate> leaveDates) {
        LocalDate today = LocalDate.now();
        Set<LocalDate> uniqueDates = new HashSet<>();
        
        for (LeaveDate leaveDate : leaveDates) {
            if (leaveDate.getDate() == null) {
                throw new IllegalArgumentException("Leave date cannot be null.");
            }
            
            // Check for past dates
            if (leaveDate.getDate().isBefore(today)) {
                throw new IllegalArgumentException(
                    "Cannot request leave for past dates. Invalid date: " + leaveDate.getDate()
                );
            }
            
            // Check for duplicate dates
            if (!uniqueDates.add(leaveDate.getDate())) {
                throw new IllegalArgumentException(
                    "Duplicate leave date found: " + leaveDate.getDate()
                );
            }
            
            // Validate shift type
            if (leaveDate.getShift() == null) {
                throw new IllegalArgumentException("Shift type is required for each leave date.");
            }
        }
    }
    
    /**
     * Validates that the leave request is submitted at least 2 working days in advance.
     * Working days exclude weekends (Saturday and Sunday).
     * 
     * @param leaveDates List of leave dates to validate
     * @throws IllegalArgumentException if advance notice requirement is not met
     */
    private void validateAdvanceNotice(List<LeaveDate> leaveDates) {
        // Find the earliest leave date
        LocalDate earliestLeaveDate = leaveDates.stream()
            .map(LeaveDate::getDate)
            .min(Comparator.naturalOrder())
            .orElseThrow(() -> new IllegalArgumentException("Leave dates cannot be empty."));
        
        // Calculate minimum required submission date (2 working days before earliest leave date)
        LocalDate minimumSubmissionDate = calculateWorkingDaysBack(earliestLeaveDate, Constants.MINIMUM_ADVANCE_NOTICE_DAYS);
        LocalDate today = LocalDate.now();
        
        if (today.isAfter(minimumSubmissionDate)) {
            throw new IllegalArgumentException(
                "Leave request must be submitted at least " + Constants.MINIMUM_ADVANCE_NOTICE_DAYS + 
                " working days in advance. " +
                "Earliest leave date: " + earliestLeaveDate + 
                ", Required submission by: " + minimumSubmissionDate + 
                ", Today: " + today
            );
        }
    }
    
    /**
     * Calculates a date that is N working days before the given date.
     * Working days exclude weekends (Saturday and Sunday).
     * 
     * @param fromDate The starting date
     * @param workingDays Number of working days to go back
     * @return The calculated date
     */
    private LocalDate calculateWorkingDaysBack(LocalDate fromDate, int workingDays) {
        LocalDate resultDate = fromDate;
        int daysToSubtract = workingDays;
        
        while (daysToSubtract > 0) {
            resultDate = resultDate.minusDays(1);
            
            // Skip weekends
            if (resultDate.getDayOfWeek() != DayOfWeek.SATURDAY && 
                resultDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                daysToSubtract--;
            }
        }
        
        return resultDate;
    }

    private BigDecimal calculateRequestedLeaveBalance(Request request) {
        BigDecimal result = BigDecimal.ZERO;

        for(var dateRange : request.getAdditionalLeaveInfo().getLeaveDates()) {
            result = result.add(dateRange.getShift().equals(ShiftType.FULL_DAY) ? BigDecimal.ONE : BigDecimal.valueOf(0.5));
        }

        return result;
    }

    private BigDecimal checkEmployeeEligibility(Request request) {
        User employee = profileQueryService.getProfileById(request.getEmployee().getUserId());
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found with ID: " + request.getEmployee().getUserId());
        }
        
        BigDecimal requestedLeaveBalance = calculateRequestedLeaveBalance(request);
        
        // Check if single request doesn't exceed reasonable limit (business rule)
        if (requestedLeaveBalance.compareTo(BigDecimal.valueOf(employee.getMaxAnnualLeave())) > 0) {
            throw new IllegalArgumentException(
                "Single leave request cannot exceed " + Constants.MAX_LEAVE_BALANCE + " days. " +
                "Requested: " + requestedLeaveBalance + " days."
            );
        }

        BigDecimal employeeRemainingBalance = employee.getRemainingAnnualLeave();
        
        if (requestedLeaveBalance.compareTo(employeeRemainingBalance) > 0) {
            throw new IllegalArgumentException(
                "Insufficient leave balance. " +
                "Requested: " + requestedLeaveBalance + " days, " +
                "Available: " + employeeRemainingBalance + " days."
            );
        }

        return requestedLeaveBalance;
    }

    @Override
    @Transactional
    public Request createRequest(Request request) {
        // Step 1: Validate request data
        validateLeaveRequest(request);

        // Step 2: Check employee eligibility and calculate requested days
        BigDecimal requestedLeaveBalance = checkEmployeeEligibility(request);

        // Step 3: Set approver and processor
        User employee = profileQueryService.getProfileById(request.getEmployee().getUserId());
        request.setApprover(User.builder().userId(employee.getManager().getUserId()).build());
        request.setProcessor(User.builder().userId(employee.getManager().getUserId()).build());
        
        // Step 4: Set initial status and total days
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        request.getAdditionalLeaveInfo().setTotalDays(requestedLeaveBalance);
        request.getAdditionalLeaveInfo().setRequest(request);

        // Step 6: Save to database
        Request createdRequest = requestDao.createRequest(request);

        // Step 7: Publish event for further processing (e.g., notifications)
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
        
        // Step 4: JPA will automatically flush changes at transaction commit
        // No need to explicitly call updateRequest() in @Transactional context

        // Step 5: Deduct leave balance from employee
        BigDecimal requestedLeaveBalance = calculateRequestedLeaveBalance(req);
        User employee = profileQueryService.getProfileById(req.getEmployee().getUserId());
        BigDecimal newRemainingBalance = employee.getRemainingAnnualLeave().subtract(requestedLeaveBalance);
        employee.setRemainingAnnualLeave(newRemainingBalance);

        timeSheetCommandService.handleLeaveApproval(employee.getUserId(), req.getAdditionalLeaveInfo().getLeaveDates());

        // Step 6: Publish event for further processing (e.g., notifications)
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
        
        // Step 5: Save updated request
        // JPA will automatically flush changes at transaction commit

        // Step 6: Publish event for further processing (e.g., notifications)
        eventPublisher.publishEvent(new RequestRejectedEvent(this, req));
        
        return req;
    }

    @Override
    public Request delegateRequest(String requestId, String newProcessorId) {
        return delegationService.delegateRequest(requestId, newProcessorId);
    }

}
