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
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.WfhDate;
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

/**
 * Service implementation for WFH (Work From Home) request operations.
 * Handles creating, approving, and rejecting WFH requests.
 */
@Service
public class WfhRequestCommandServiceImpl implements RequestCommandService {

    private static final int MINIMUM_ADVANCE_NOTICE_DAYS = 2;

    private final RequestDao requestDao;
    private final ProfileQueryService profileQueryService;
    private final RequestValidationService requestValidationService;
    private final ApplicationEventPublisher eventPublisher;
    private final RequestDelegationService delegationService;
    private final TimeSheetCommandService timeSheetCommandService;

    public WfhRequestCommandServiceImpl(
            RequestDao requestDao,
            ProfileQueryService profileQueryService,
            ApplicationEventPublisher eventPublisher,
            RequestDelegationService delegationService,
            RequestValidationService requestValidationService,
            TimeSheetCommandService timeSheetCommandService) {
        this.requestDao = requestDao;
        this.profileQueryService = profileQueryService;
        this.eventPublisher = eventPublisher;
        this.requestValidationService = requestValidationService;
        this.delegationService = delegationService;
        this.timeSheetCommandService = timeSheetCommandService;
    }

    private void validateWfhRequest(Request request) {
        // Validate request type
        if (request.getRequestType() != null && request.getRequestType() != RequestType.WFH) {
            throw new IllegalArgumentException("Invalid request type for WFH request.");
        }

        // Validate title
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Request title is required for WFH request creation.");
        }

        // Validate user reason
        if (request.getUserReason() == null || request.getUserReason().trim().isEmpty()) {
            throw new IllegalArgumentException("User reason is required for WFH request creation.");
        }

        // Validate employee information
        if (request.getEmployee() == null || request.getEmployee().getUserId() == null) {
            throw new IllegalArgumentException("Employee information is required for WFH request creation.");
        }

        // Validate additional WFH info
        if (request.getAdditionalWfhInfo() == null) {
            throw new IllegalArgumentException("Additional WFH information is required for WFH request creation.");
        }

        // Validate WFH dates
        List<WfhDate> wfhDates = request.getAdditionalWfhInfo().getWfhDates();
        if (wfhDates == null || wfhDates.isEmpty()) {
            throw new IllegalArgumentException("WFH dates cannot be empty for WFH request creation.");
        }

        // Validate each WFH date individually
        validateEachWfhDate(wfhDates);

        // Validate advance notice requirement (must submit at least 2 working days
        // before)
        validateAdvanceNotice(wfhDates);
    }

    private void validateEachWfhDate(List<WfhDate> wfhDates) {
        LocalDate today = LocalDate.now();
        Set<LocalDate> uniqueDates = new HashSet<>();

        for (WfhDate wfhDate : wfhDates) {
            if (wfhDate.getDate() == null) {
                throw new IllegalArgumentException("WFH date cannot be null.");
            }

            // Check for past dates
            if (wfhDate.getDate().isBefore(today)) {
                throw new IllegalArgumentException(
                        "Cannot request WFH for past dates. Invalid date: " + wfhDate.getDate());
            }

            // Check for duplicate dates
            if (!uniqueDates.add(wfhDate.getDate())) {
                throw new IllegalArgumentException(
                        "Duplicate WFH date found: " + wfhDate.getDate());
            }

            // Validate shift type
            if (wfhDate.getShift() == null) {
                throw new IllegalArgumentException("Shift type is required for each WFH date.");
            }
        }
    }

    /**
     * Validates that the WFH request is submitted at least 2 working days in
     * advance.
     * Working days exclude weekends (Saturday and Sunday).
     */
    private void validateAdvanceNotice(List<WfhDate> wfhDates) {
        // Find the earliest WFH date
        LocalDate earliestWfhDate = wfhDates.stream()
                .map(WfhDate::getDate)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalArgumentException("WFH dates cannot be empty."));

        // Calculate minimum required submission date
        LocalDate minimumSubmissionDate = calculateWorkingDaysBack(earliestWfhDate, MINIMUM_ADVANCE_NOTICE_DAYS);
        LocalDate today = LocalDate.now();

        if (today.isAfter(minimumSubmissionDate)) {
            throw new IllegalArgumentException(
                    "WFH request must be submitted at least " + MINIMUM_ADVANCE_NOTICE_DAYS +
                            " working days in advance. " +
                            "Earliest WFH date: " + earliestWfhDate +
                            ", Required submission by: " + minimumSubmissionDate +
                            ", Today: " + today);
        }
    }

    /**
     * Calculates a date that is N working days before the given date.
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

    private BigDecimal calculateRequestedWfhDays(Request request) {
        BigDecimal result = BigDecimal.ZERO;

        for (var wfhDate : request.getAdditionalWfhInfo().getWfhDates()) {
            result = result.add(wfhDate.getShift().equals(ShiftType.FULL_DAY)
                    ? BigDecimal.ONE
                    : BigDecimal.valueOf(0.5));
        }

        return result;
    }

    /**
     * Check employee eligibility for WFH request.
     * Validates that the employee has sufficient WFH days remaining.
     * 
     * @param request  The WFH request to validate
     * @param employee The employee making the request
     * @return The calculated WFH days for this request
     */
    private BigDecimal checkEmployeeEligibility(Request request, User employee) {
        BigDecimal requestedWfhDays = calculateRequestedWfhDays(request);

        // Check if single request doesn't exceed maximum allowed WFH days
        if (requestedWfhDays.compareTo(employee.getMaxWfhDays()) > 0) {
            throw new IllegalArgumentException(
                    "Single WFH request cannot exceed " + employee.getMaxWfhDays() + " days. " +
                            "Requested: " + requestedWfhDays + " days.");
        }

        BigDecimal employeeRemainingWfhDays = employee.getRemainingWfhDays();

        // Check if employee has sufficient WFH balance
        if (requestedWfhDays.compareTo(employeeRemainingWfhDays) > 0) {
            throw new IllegalArgumentException(
                    "Insufficient WFH days balance. " +
                            "Requested: " + requestedWfhDays + " days, " +
                            "Available: " + employeeRemainingWfhDays + " days.");
        }

        return requestedWfhDays;
    }

    /**
     * Set up bidirectional relationships for WFH request entities.
     * This should be done in service layer, not in DTOs.
     */
    private void setupBidirectionalRelationships(Request request) {
        // Set relationship: AdditionalWfhInfo <-> Request
        request.getAdditionalWfhInfo().setRequest(request);

        // Set relationship: WfhDate <-> AdditionalWfhInfo
        for (var wfhDate : request.getAdditionalWfhInfo().getWfhDates()) {
            wfhDate.setAdditionalWfhInfo(request.getAdditionalWfhInfo());
        }
    }

    @Override
    @Transactional
    public Request createRequest(Request request) {
        // Step 1: Validate request data
        validateWfhRequest(request);

        // Step 2: Fetch employee info
        User employee = profileQueryService.getProfileById(request.getEmployee().getUserId());
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found with ID: " + request.getEmployee().getUserId());
        }

        // Step 3: Check employee eligibility and calculate requested days
        BigDecimal requestedWfhDays = checkEmployeeEligibility(request, employee);

        // Step 4: Set employee, approver and processor
        request.setEmployee(employee);
        request.setApprover(employee.getManager());
        request.setProcessor(employee.getManager());

        // Step 5: Set initial status, type and total days
        request.setStatus(RequestStatus.PENDING);
        request.setRequestType(RequestType.WFH);
        request.setCreatedAt(LocalDateTime.now());
        request.getAdditionalWfhInfo().setTotalDays(requestedWfhDays);

        // Step 6: Set up bidirectional relationships
        setupBidirectionalRelationships(request);

        // Step 7: Save to database
        Request createdRequest = requestDao.createRequest(request);

        // Step 8: Publish event for further processing (e.g., notifications)
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

        // Step 4: Update wfh remaining balance
        User employee = req.getEmployee();
        BigDecimal requestedWfhBalance = calculateRequestedWfhDays(req);
        BigDecimal newWfhBalance = employee.getRemainingWfhDays().subtract(requestedWfhBalance);
        employee.setRemainingWfhDays(newWfhBalance);

        // Step 4: Update time sheet
        timeSheetCommandService.handleWfhApproval(req.getEmployee().getUserId(),
                req.getAdditionalWfhInfo().getWfhDates());

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

        // Step 5: Publish event for further processing (e.g., notifications)
        eventPublisher.publishEvent(new RequestRejectedEvent(this, req));

        return req;
    }

    @Override
    public Request delegateRequest(String requestId, String newProcessorId) {
        return delegationService.delegateRequest(requestId, newProcessorId);
    }
}
