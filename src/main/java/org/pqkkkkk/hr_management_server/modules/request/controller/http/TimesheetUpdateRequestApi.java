package org.pqkkkkk.hr_management_server.modules.request.controller.http;

import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response.ApiResponse;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.DTO.TimesheetUpdateRequestDTO;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.Request.CreateTimesheetUpdateRequestRequest;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestCommandService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/requests/timesheet-update")
public class TimesheetUpdateRequestApi {
    private final RequestCommandService timesheetUpdateRequestCommandService;

    public TimesheetUpdateRequestApi(
        @Qualifier("timesheetUpdateRequestCommandService") RequestCommandService timesheetUpdateRequestCommandService
    ) {
        this.timesheetUpdateRequestCommandService = timesheetUpdateRequestCommandService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TimesheetUpdateRequestDTO>> createTimesheetUpdateRequest(
            @Valid @RequestBody CreateTimesheetUpdateRequestRequest request) {

        // Convert DTO to entity        
        Request requestentity = request.toEntity();

        // Create request through service
        Request createdRequest = timesheetUpdateRequestCommandService.createRequest(requestentity);

        // Convert created entity to DTO
        TimesheetUpdateRequestDTO responseDTO = TimesheetUpdateRequestDTO.fromEntity(createdRequest);

        // Build response
        ApiResponse<TimesheetUpdateRequestDTO> apiResponse = new ApiResponse<>(
                responseDTO,
                true,
                201,
                "Timesheet update request created successfully.",
                null
        );
        return ResponseEntity.status(201).body(apiResponse);
    }
}
