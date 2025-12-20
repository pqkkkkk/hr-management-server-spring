package org.pqkkkkk.hr_management_server.modules.request.controller.http;

import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response.ApiResponse;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.DTO.CheckOutRequestDTO;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.Request.CreateCheckOutRequestRequest;
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
@RequestMapping("/api/v1/requests/check-out")
public class CheckOutRequestApi {
    private final RequestCommandService checkOutRequestCommandService;
    private final RequestQueryService requestQueryService;

    public CheckOutRequestApi(
            @Qualifier("checkOutRequestCommandService") RequestCommandService checkOutRequestCommandService,
            RequestQueryService requestQueryService) {
        this.checkOutRequestCommandService = checkOutRequestCommandService;
        this.requestQueryService = requestQueryService;
    }

    /**
     * GET /api/v1/requests/check-out/{requestId}
     * Get a check-out request by ID
     * 
     * @param requestId - the ID of the request
     * @return Check-out request details
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<CheckOutRequestDTO>> getCheckOutRequest(@PathVariable String requestId) {
        Request request = requestQueryService.getRequestById(requestId);

        if (request.getRequestType() != RequestType.CHECK_OUT) {
            throw new IllegalArgumentException("Request with ID " + requestId + " is not a CHECK_OUT request");
        }

        CheckOutRequestDTO responseDTO = CheckOutRequestDTO.fromEntity(request);

        ApiResponse<CheckOutRequestDTO> apiResponse = new ApiResponse<>(
                responseDTO,
                true,
                200,
                "Check-out request retrieved successfully.",
                null);

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CheckOutRequestDTO>> createCheckOutRequest(
            @Valid @RequestBody CreateCheckOutRequestRequest request) {
        // Convert DTO to entity
        var requestEntity = request.toEntity();

        // Create request through service
        var createdRequest = checkOutRequestCommandService.createRequest(requestEntity);

        // Convert created entity to DTO
        var responseDTO = CheckOutRequestDTO.fromEntity(createdRequest);

        // Build response
        ApiResponse<CheckOutRequestDTO> apiResponse = new ApiResponse<>(
                responseDTO,
                true,
                201,
                "Check-out request created successfully.",
                null);

        return ResponseEntity.status(201).body(apiResponse);
    }
}
