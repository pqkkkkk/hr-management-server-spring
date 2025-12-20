package org.pqkkkkk.hr_management_server.modules.request.controller.http;

import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.DTO.LeaveRequestDTO;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.Request.CreateLeaveRequestRequest;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.Response.ApiResponse;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestCommandService;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestQueryService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * REST API controller for Leave Request management.
 * Provides endpoints for creating, approving, rejecting, and querying leave
 * requests.
 */
@RestController
@RequestMapping("/api/v1/requests/leave")
public class LeaveRequestApi {

        private final RequestCommandService leaveRequestCommandService;
        private final RequestQueryService requestQueryService;

        public LeaveRequestApi(
                        @Qualifier("leaveRequestCommandServiceImpl") RequestCommandService leaveRequestCommandService,
                        RequestQueryService requestQueryService) {
                this.leaveRequestCommandService = leaveRequestCommandService;
                this.requestQueryService = requestQueryService;
        }

        /**
         * POST /api/v1/requests/leave
         * Create a new leave request
         * 
         * @param request - the leave request data
         * @return Created leave request
         */
        @PostMapping
        public ResponseEntity<ApiResponse<LeaveRequestDTO>> createLeaveRequest(
                        @Valid @RequestBody CreateLeaveRequestRequest request) {

                // Convert DTO to entity
                Request requestEntity = request.toEntity();

                // Create leave request through service
                Request createdRequest = leaveRequestCommandService.createRequest(requestEntity);

                // Convert entity to DTO
                LeaveRequestDTO leaveRequestDTO = LeaveRequestDTO.fromEntity(createdRequest);

                // Build response
                ApiResponse<LeaveRequestDTO> apiResponse = new ApiResponse<>(
                                leaveRequestDTO,
                                true,
                                HttpStatus.CREATED.value(),
                                "Leave request created successfully.",
                                null);

                return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        }

        /**
         * GET /api/v1/requests/leave/{requestId}
         * Get a leave request by ID
         * 
         * @param requestId - the ID of the request
         * @return Leave request details
         */
        @GetMapping("/{requestId}")
        public ResponseEntity<ApiResponse<LeaveRequestDTO>> getLeaveRequest(@PathVariable String requestId) {
                Request request = requestQueryService.getRequestById(requestId);

                if (request.getRequestType() != RequestType.LEAVE) {
                        throw new IllegalArgumentException("Request with ID " + requestId + " is not a LEAVE request");
                }

                LeaveRequestDTO leaveRequestDTO = LeaveRequestDTO.fromEntity(request);

                ApiResponse<LeaveRequestDTO> apiResponse = new ApiResponse<>(
                                leaveRequestDTO,
                                true,
                                HttpStatus.OK.value(),
                                "Leave request retrieved successfully.",
                                null);

                return ResponseEntity.ok(apiResponse);
        }
}
