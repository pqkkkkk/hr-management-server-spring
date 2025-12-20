package org.pqkkkkk.hr_management_server.modules.request.controller.http;

import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.DTO.WfhRequestDTO;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.Request.CreateWfhRequestRequest;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.Response.ApiResponse;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
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
 * REST API controller for WFH (Work From Home) Request management.
 * Provides endpoints for creating work from home requests.
 */
@RestController
@RequestMapping("/api/v1/requests/wfh")
public class WfhRequestApi {

        private final RequestCommandService wfhRequestCommandService;
        private final RequestQueryService requestQueryService;

        public WfhRequestApi(
                        @Qualifier("wfhRequestCommandServiceImpl") RequestCommandService wfhRequestCommandService,
                        RequestQueryService requestQueryService) {
                this.wfhRequestCommandService = wfhRequestCommandService;
                this.requestQueryService = requestQueryService;
        }

        /**
         * GET /api/v1/requests/wfh/{requestId}
         * Get a WFH request by ID
         * 
         * @param requestId - the ID of the request
         * @return WFH request details
         */
        @GetMapping("/{requestId}")
        public ResponseEntity<ApiResponse<WfhRequestDTO>> getWfhRequest(@PathVariable String requestId) {
                Request request = requestQueryService.getRequestById(requestId);

                if (request.getRequestType() != RequestType.WFH) {
                        throw new IllegalArgumentException("Request with ID " + requestId + " is not a WFH request");
                }

                WfhRequestDTO wfhRequestDTO = WfhRequestDTO.fromEntity(request);

                ApiResponse<WfhRequestDTO> apiResponse = new ApiResponse<>(
                                wfhRequestDTO,
                                true,
                                HttpStatus.OK.value(),
                                "WFH request retrieved successfully.",
                                null);

                return ResponseEntity.ok(apiResponse);
        }

        /**
         * POST /api/v1/requests/wfh
         * Create a new work from home request
         * 
         * @param request - the WFH request data
         * @return Created WFH request
         */
        @PostMapping
        public ResponseEntity<ApiResponse<WfhRequestDTO>> createWfhRequest(
                        @Valid @RequestBody CreateWfhRequestRequest request) {

                // Convert DTO to entity
                Request requestEntity = request.toEntity();

                // Create WFH request through service
                Request createdRequest = wfhRequestCommandService.createRequest(requestEntity);

                // Convert entity to DTO
                WfhRequestDTO wfhRequestDTO = WfhRequestDTO.fromEntity(createdRequest);

                // Build response
                ApiResponse<WfhRequestDTO> apiResponse = new ApiResponse<>(
                                wfhRequestDTO,
                                true,
                                HttpStatus.CREATED.value(),
                                "WFH request created successfully.",
                                null);

                return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        }
}
