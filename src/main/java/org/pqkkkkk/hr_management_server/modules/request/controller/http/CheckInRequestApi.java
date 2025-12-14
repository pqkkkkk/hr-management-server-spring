package org.pqkkkkk.hr_management_server.modules.request.controller.http;

import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.DTO.CheckInRequestDTO;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.Request.CreateCheckInRequestRequest;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.Response.ApiResponse;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestCommandService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/requests/check-in")
public class CheckInRequestApi {
    private final RequestCommandService checkInRequestCommandService;

    public CheckInRequestApi(@Qualifier("checkInRequestCommandService") RequestCommandService checkInRequestCommandService) {
        this.checkInRequestCommandService = checkInRequestCommandService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CheckInRequestDTO>> createCheckInRequest(
            @RequestParam Long employeeId,
            @Valid @RequestBody CreateCheckInRequestRequest request) {
        // Convert DTO to entity
        Request requestEntity = request.toEntity(employeeId);
        // Gọi service createCheckInRequest
        Request createdRequest = checkInRequestCommandService.createCheckInRequest(requestEntity);
        // Convert entity sang DTO
        CheckInRequestDTO responseDTO = CheckInRequestDTO.fromEntity(createdRequest);
        // Build response
        ApiResponse<CheckInRequestDTO> apiResponse = new ApiResponse<>(
                responseDTO,
                true,
                201,
                "Check-in request created successfully.",
                null
        );
        return ResponseEntity.status(201).body(apiResponse);
    }
}
