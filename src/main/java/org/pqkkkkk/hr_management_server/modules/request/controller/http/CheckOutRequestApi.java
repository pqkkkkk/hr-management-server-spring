package org.pqkkkkk.hr_management_server.modules.request.controller.http;

import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response.ApiResponse;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.DTO.CheckOutRequestDTO;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.Request.CreateCheckOutRequestRequest;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestCommandService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/requests/check-out")
public class CheckOutRequestApi {
    private final RequestCommandService checkOutRequestCommandService;

    public CheckOutRequestApi(@Qualifier("checkOutRequestCommandService") RequestCommandService checkOutRequestCommandService) {
        this.checkOutRequestCommandService = checkOutRequestCommandService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CheckOutRequestDTO>> createCheckOutRequest(@Valid @RequestBody CreateCheckOutRequestRequest request) 
    {
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
                null
        );

        return ResponseEntity.status(201).body(apiResponse);
    }
}
