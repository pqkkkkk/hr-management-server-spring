package org.pqkkkkk.hr_management_server.modules.request.controller.http;

import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response.ApiResponse;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.DTO.CheckInRequestDTO;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.Request.CreateCheckInRequestRequest;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestCommandService;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestQueryService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/requests/check-in")
public class CheckInRequestApi {
    private final RequestCommandService checkInRequestCommandService;
    private final RequestQueryService requestQueryService;

    public CheckInRequestApi(
            @Qualifier("checkInRequestCommandService") RequestCommandService checkInRequestCommandService,
            RequestQueryService requestQueryService) {
        this.checkInRequestCommandService = checkInRequestCommandService;
        this.requestQueryService = requestQueryService;
    }

    /**
     * GET /api/v1/requests/check-in/{requestId}
     * Get a check-in request by ID
     * 
     * @param requestId - the ID of the request
     * @return Check-in request details
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<CheckInRequestDTO>> getCheckInRequest(@PathVariable String requestId) {
        Request request = requestQueryService.getRequestById(requestId);

        if (request.getRequestType() != RequestType.CHECK_IN) {
            throw new IllegalArgumentException("Request with ID " + requestId + " is not a CHECK_IN request");
        }

        CheckInRequestDTO responseDTO = CheckInRequestDTO.fromEntity(request);

        ApiResponse<CheckInRequestDTO> apiResponse = new ApiResponse<>(
                responseDTO,
                true,
                200,
                "Check-in request retrieved successfully.",
                null);

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CheckInRequestDTO>> createCheckInRequest(
            @Valid @RequestBody CreateCheckInRequestRequest request) {
        // Convert DTO to entity
        var requestEntity = request.toEntity();

        // Create request through service
        var createdRequest = checkInRequestCommandService.createRequest(requestEntity);

        // Convert created entity to DTO
        var responseDTO = CheckInRequestDTO.fromEntity(createdRequest);

        // Build response
        ApiResponse<CheckInRequestDTO> apiResponse = new ApiResponse<>(
                responseDTO,
                true,
                201,
                "Check-in request created successfully.",
                null);

        return ResponseEntity.status(201).body(apiResponse);
    }

}
