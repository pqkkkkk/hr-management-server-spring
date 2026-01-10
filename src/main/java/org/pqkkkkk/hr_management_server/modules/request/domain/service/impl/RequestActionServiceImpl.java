package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.BulkApproveResult;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.filter.FilterCriteria.RequestFilter;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestActionService;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestCommandService;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestQueryService;
import org.pqkkkkk.hr_management_server.shared.Constants.SortDirection;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

/**
 * Implementation of RequestActionService that provides unified approve and
 * reject
 * functionality for all request types.
 * <p>
 * This service determines the request type and delegates the action to the
 * appropriate RequestCommandService implementation.
 */
@Service
public class RequestActionServiceImpl implements RequestActionService {

    private final List<RequestCommandService> commandServices;
    private final RequestQueryService queryService;

    public RequestActionServiceImpl(
            List<RequestCommandService> commandServices,
            RequestQueryService queryService) {
        this.commandServices = commandServices;
        this.queryService = queryService;
    }

    @Override
    @Transactional
    public Request approve(String requestId, String approverId) {
        // Get request to determine type
        Request request = queryService.getRequestById(requestId);
        if (request == null) {
            throw new IllegalArgumentException("Request not found with ID: " + requestId);
        }

        // Find appropriate service for this request type
        RequestCommandService service = findServiceForType(request.getRequestType());

        // Delegate to the appropriate service
        return service.approveRequest(requestId, approverId);
    }

    @Override
    @Transactional
    public Request reject(String requestId, String rejecterId, String rejectionReason) {
        // Get request to determine type
        Request request = queryService.getRequestById(requestId);
        if (request == null) {
            throw new IllegalArgumentException("Request not found with ID: " + requestId);
        }

        // Find appropriate service for this request type
        RequestCommandService service = findServiceForType(request.getRequestType());

        // Delegate to the appropriate service
        return service.rejectRequest(requestId, rejecterId, rejectionReason);
    }

    /**
     * Finds the appropriate RequestCommandService implementation for the given
     * request type.
     * 
     * @param requestType The type of request (CHECK_IN, CHECK_OUT, LEAVE, etc.)
     * @return The matching RequestCommandService implementation
     * @throws IllegalArgumentException if no service supports the given request
     *                                  type
     */
    private RequestCommandService findServiceForType(RequestType requestType) {
        String expectedServiceName = getServiceNameForType(requestType);

        return commandServices.stream()
                .filter(service -> service.getClass().getSimpleName().startsWith(expectedServiceName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No service found for request type: " + requestType));
    }

    /**
     * Gets the expected service class name prefix for a given request type.
     * 
     * @param requestType The request type
     * @return The expected service name prefix (e.g., "Leave" for LEAVE type)
     */
    private String getServiceNameForType(RequestType requestType) {
        switch (requestType) {
            case LEAVE:
                return "Leave";
            case CHECK_IN:
                return "CheckIn";
            case CHECK_OUT:
                return "CheckOut";
            case WFH:
                return "Wfh";
            case TIMESHEET:
                return "Timesheet";
            default:
                throw new IllegalArgumentException("Unsupported request type: " + requestType);
        }
    }

    @Override
    @Transactional
    public Request delegate(String requestId, String newProcessorId) {
        // Get request to determine type
        Request request = queryService.getRequestById(requestId);
        if (request == null) {
            throw new IllegalArgumentException("Request not found with ID: " + requestId);
        }

        // Find appropriate service for this request type
        RequestCommandService service = findServiceForType(request.getRequestType());

        // Delegate to the appropriate service
        return service.delegateRequest(requestId, newProcessorId);
    }

    @Override
    @Transactional
    public BulkApproveResult bulkApprove(RequestFilter filter, String approverId, int maxRequests) {
        // Validate inputs
        if (approverId == null || approverId.isBlank()) {
            throw new IllegalArgumentException("Approver ID is required");
        }
        if (maxRequests <= 0) {
            throw new IllegalArgumentException("Max requests must be positive");
        }

        // Create filter with PENDING status and appropriate page size
        RequestFilter pendingFilter = new RequestFilter(
                filter.employeeId(),
                filter.approverId(), // Filter by manager who has authority to approve
                filter.processorId(), // Filter by delegated processor
                filter.departmentId(),
                filter.nameTerm(),
                RequestStatus.PENDING, // Always filter by PENDING status
                filter.type(),
                filter.startDate(),
                filter.endDate(),
                1, // First page
                maxRequests, // Page size = max requests
                "createdAt",
                SortDirection.ASC);

        // Get pending requests
        Page<Request> pendingRequests = queryService.getRequests(pendingFilter);

        List<String> approvedRequestIds = new ArrayList<>();
        List<BulkApproveResult.FailedApproval> failedApprovals = new ArrayList<>();

        // Process each request
        for (Request request : pendingRequests.getContent()) {
            try {
                // Attempt to approve
                approve(request.getRequestId(), approverId);
                approvedRequestIds.add(request.getRequestId());
            } catch (Exception e) {
                // Collect failure info and continue
                String employeeName = request.getEmployee() != null
                        ? request.getEmployee().getFullName()
                        : "Unknown";
                failedApprovals.add(new BulkApproveResult.FailedApproval(
                        request.getRequestId(),
                        employeeName,
                        e.getMessage()));
            }
        }

        return new BulkApproveResult(
                pendingRequests.getContent().size(),
                approvedRequestIds.size(),
                approvedRequestIds,
                failedApprovals);
    }
}
