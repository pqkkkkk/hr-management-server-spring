package org.pqkkkkk.hr_management_server.modules.request.controller.http;

import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response.ApiResponse;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.DTO.RequestDTO;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.filter.FilterCriteria.RequestFilter;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestQueryService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/requests")
public class RequestApi {
    private final RequestQueryService requestQueryService;

    public RequestApi(RequestQueryService requestQueryService) {
        this.requestQueryService = requestQueryService;
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
}
