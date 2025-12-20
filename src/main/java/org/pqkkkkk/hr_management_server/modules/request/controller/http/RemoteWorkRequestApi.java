package org.pqkkkkk.hr_management_server.modules.request.controller.http;

import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response.ApiResponse;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.DTO.RemoteWorkRequestDTO;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.Request.CreateRemoteWorkRequestRequest;
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
@RequestMapping("/api/v1/requests/remote-work")
public class RemoteWorkRequestApi {
    private final RequestCommandService remoteWorkRequestCommandService;

    public RemoteWorkRequestApi(
        @Qualifier("remoteWorkRequestCommandService") RequestCommandService remoteWorkRequestCommandService
    ) {
        this.remoteWorkRequestCommandService = remoteWorkRequestCommandService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RemoteWorkRequestDTO>> createRemoteWorkRequest(
            @Valid @RequestBody CreateRemoteWorkRequestRequest request) {

        // Convert DTO to entity        
        Request requestEntity = request.toEntity();

        // Create request through service
        Request createdRequest = remoteWorkRequestCommandService.createRequest(requestEntity);

        // Convert created entity to DTO
        RemoteWorkRequestDTO responseDTO = RemoteWorkRequestDTO.fromEntity(createdRequest);

        // Build response
        ApiResponse<RemoteWorkRequestDTO> apiResponse = new ApiResponse<>(
                responseDTO,
                true,
                201,
                "Remote work request created successfully.",
                null
        );
        return ResponseEntity.status(201).body(apiResponse);
    }
}
