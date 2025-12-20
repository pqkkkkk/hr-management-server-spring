package org.pqkkkkk.hr_management_server.modules.request.controller.http;

import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.DTO.TimeSheetRequestDTO;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.Request.CreateTimeSheetRequestRequest;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.Response.ApiResponse;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestCommandService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * REST API controller for Timesheet Request management.
 * Provides endpoints for creating timesheet correction requests.
 */
@RestController
@RequestMapping("/api/v1/requests/timesheet")
public class TimeSheetRequestApi {

        private final RequestCommandService timeSheetRequestCommandService;

        public TimeSheetRequestApi(
                        @Qualifier("timesheetRequestCommandServiceImpl") RequestCommandService timeSheetRequestCommandService) {
                this.timeSheetRequestCommandService = timeSheetRequestCommandService;
        }

        /**
         * POST /api/v1/requests/timesheet
         * Create a new timesheet correction request
         * 
         * @param request - the timesheet request data
         * @return Created timesheet request
         */
        @PostMapping
        public ResponseEntity<ApiResponse<TimeSheetRequestDTO>> createTimeSheetRequest(
                        @Valid @RequestBody CreateTimeSheetRequestRequest request) {

                // Convert DTO to entity
                Request requestEntity = request.toEntity();

                // Create timesheet request through service
                Request createdRequest = timeSheetRequestCommandService.createRequest(requestEntity);

                // Convert entity to DTO
                TimeSheetRequestDTO timeSheetRequestDTO = TimeSheetRequestDTO.fromEntity(createdRequest);

                // Build response
                ApiResponse<TimeSheetRequestDTO> apiResponse = new ApiResponse<>(
                                timeSheetRequestDTO,
                                true,
                                HttpStatus.CREATED.value(),
                                "Timesheet request created successfully.",
                                null);

                return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        }
}
