package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import java.util.List;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestActionService;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestCommandService;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestQueryService;
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
}
