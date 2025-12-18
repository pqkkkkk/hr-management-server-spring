package org.pqkkkkk.hr_management_server.modules.request.controller.http;

import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response.ApiResponse;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.DTO.RequestDTO;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.Request.ApproveRequestRequest;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.Request.DelegateRequestRequest;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.Request.RejectRequestRequest;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.filter.FilterCriteria.RequestFilter;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestActionService;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestQueryService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/requests")
public class RequestApi {
    private final RequestQueryService requestQueryService;
    private final RequestActionService requestActionService;

    public RequestApi(
            RequestQueryService requestQueryService,
            RequestActionService requestActionService
    ) {
        this.requestQueryService = requestQueryService;
        this.requestActionService = requestActionService;
    }

        @GetMapping("/my-requests")
    public ResponseEntity<ApiResponse<Page<RequestDTO>>> getMyLeaveRequests(
            @Valid @ModelAttribute RequestFilter filter) {
        
        // Validate employeeId is required
        if (filter.employeeId() == null || filter.employeeId().isBlank()) {
            throw new IllegalArgumentException("employeeId is required and cannot be empty");
        }
        
        // Get requests through service
        Page<Request> requests = requestQueryService.getRequests(filter);
        
        // Convert entities to DTOs
        Page<RequestDTO> requestDTOs = requests.map(RequestDTO::fromEntity);
        
        // Build response
        ApiResponse<Page<RequestDTO>> apiResponse = new ApiResponse<>(
                requestDTOs,
                true,
                HttpStatus.OK.value(),
                "My requests retrieved successfully.",
                null
        );
        
        return ResponseEntity.ok(apiResponse);
    }
    
    @GetMapping("/team-requests")
    public ResponseEntity<ApiResponse<Page<RequestDTO>>> getTeamLeaveRequests(
            @Valid @ModelAttribute RequestFilter filter) {
        
        // Validate approverId is required
        if (filter.approverId() == null || filter.approverId().isBlank()) {
            throw new IllegalArgumentException("approverId is required and cannot be empty");
        }
        
        // Get requests through service
        Page<Request> requests = requestQueryService.getRequests(filter);
        
        // Convert entities to DTOs
        Page<RequestDTO> requestDTOs = requests.map(RequestDTO::fromEntity);
        
        // Build response
        ApiResponse<Page<RequestDTO>> apiResponse = new ApiResponse<>(
                requestDTOs,
                true,
                HttpStatus.OK.value(),
                "Team requests retrieved successfully.",
                null
        );
        
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * Approve a request of any type.
     * <p>
     * This endpoint works for all request types (CHECK_IN, CHECK_OUT, LEAVE, etc.).
     * The system will automatically determine the request type and apply the
     * appropriate approval logic.
     * 
     * @param requestId The ID of the request to approve
     * @param approverId The ID of the user approving (from auth context or request header)
     * @return The approved request details
     */
    @PatchMapping("/{requestId}/approve")
    public ResponseEntity<ApiResponse<RequestDTO>> approveRequest(
            @PathVariable String requestId, @Valid @RequestBody ApproveRequestRequest req) {
        
        // Approve request through service
        Request approvedRequest = requestActionService.approve(requestId, req.approverId());
        
        // Convert entity to DTO
        RequestDTO requestDTO = RequestDTO.fromEntity(approvedRequest);
        
        // Build response
        ApiResponse<RequestDTO> apiResponse = new ApiResponse<>(
                requestDTO,
                true,
                HttpStatus.OK.value(),
                "Request approved successfully.",
                null
        );
        
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * Reject a request of any type.
     * <p>
     * This endpoint works for all request types (CHECK_IN, CHECK_OUT, LEAVE, etc.).
     * The system will automatically determine the request type and apply the
     * appropriate rejection logic.
     * 
     * @param requestId The ID of the request to reject
     * @param request The rejection details including reason
     * @return The rejected request details
     */
    @PatchMapping("/{requestId}/reject")
    public ResponseEntity<ApiResponse<RequestDTO>> rejectRequest(
            @PathVariable String requestId,
            @Valid @RequestBody RejectRequestRequest request) {

        
        // Reject request through service
        Request rejectedRequest = requestActionService.reject(
                requestId, 
                request.rejecterId(), 
                request.rejectReason()
        );
        
        // Convert entity to DTO
        RequestDTO requestDTO = RequestDTO.fromEntity(rejectedRequest);
        
        // Build response
        ApiResponse<RequestDTO> apiResponse = new ApiResponse<>(
                requestDTO,
                true,
                HttpStatus.OK.value(),
                "Request rejected successfully.",
                null
        );
        
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * Delegate a request to another processor.
     * <p>
     * This endpoint allows managers/HR to delegate a pending request to another
     * authorized processor (ADMIN or HR role).
     * 
     * @param requestId The ID of the request to delegate
     * @param request The delegation details including new processor ID
     * @return The delegated request details
     */
    @PatchMapping("/{requestId}/delegate")
    public ResponseEntity<ApiResponse<RequestDTO>> delegateRequest(
            @PathVariable String requestId,
            @Valid @RequestBody DelegateRequestRequest request) {
        
        // Delegate request through action service
        Request delegatedRequest = requestActionService.delegate(
                requestId, 
                request.newProcessorId()
        );
        
        // Convert entity to DTO
        RequestDTO requestDTO = RequestDTO.fromEntity(delegatedRequest);
        
        // Build response
        ApiResponse<RequestDTO> apiResponse = new ApiResponse<>(
                requestDTO,
                true,
                HttpStatus.OK.value(),
                "Request delegated successfully.",
                null
        );
        
        return ResponseEntity.ok(apiResponse);
    }
}
