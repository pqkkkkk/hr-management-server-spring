package org.pqkkkkk.hr_management_server.modules.request.controller.http;

import java.security.Principal;
import java.time.LocalDate;

import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response.ApiResponse;
import org.pqkkkkk.hr_management_server.modules.request.controller.http.dto.LeaveRequestDTO;
import org.pqkkkkk.hr_management_server.modules.request.domain.command.LeaveRequestFilterCommand;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.LeaveRequestQueryService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/requests/leave")
@RequiredArgsConstructor
public class LeaveRequestApi {

    private final LeaveRequestQueryService leaveRequestQueryService;

    @GetMapping("/my-requests")
    public ResponseEntity<ApiResponse<Page<LeaveRequestDTO>>> getMyLeaveRequests(
            Principal principal,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) RequestType requestType,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        // Extract employeeId từ authentication context (principal name)
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(null, false, HttpStatus.UNAUTHORIZED.value(),
                        "Unauthorized", null));
        }
        String employeeId = principal.getName();

        LeaveRequestFilterCommand filter = LeaveRequestFilterCommand.builder()
            .employeeId(employeeId)
            .status(status)
            .requestType(requestType)
            .startDate(startDate)
            .endDate(endDate)
            .page(page)
            .size(size)
            .build();

        Page<Request> requests = leaveRequestQueryService.getMyLeaveRequests(filter);
        Page<LeaveRequestDTO> dtos = requests.map(LeaveRequestDTO::fromEntity);

        ApiResponse<Page<LeaveRequestDTO>> response = new ApiResponse<>(dtos, true,
            HttpStatus.OK.value(), "Leave requests retrieved successfully", null);

        return ResponseEntity.ok(response);
    }
}