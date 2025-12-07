package org.pqkkkkk.hr_management_server.modules.request.controller.http.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.AdditionalLeaveInfo;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.LeaveType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.ShiftType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.LeaveDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTOs for Request module (Leave Requests).
 * <p> They are used to encapsulate data sent by clients in HTTP requests.
 * <p> Each request DTO includes methods to convert to entity objects.
 */
public class Request {
    
    /**
     * Request DTO for creating a leave request
     */
    public record CreateLeaveRequestRequest(
            @NotBlank(message = "Title is required")
            String title,
            
            @NotBlank(message = "User reason is required")
            String userReason,
            
            String attachmentUrl,
            
            @NotBlank(message = "Employee ID is required")
            String employeeId,
            
            @NotNull(message = "Leave type is required")
            LeaveType leaveType,
            
            @NotEmpty(message = "Leave dates cannot be empty")
            @Valid
            List<LeaveDateRequest> leaveDates
    ) {
        public org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request toEntity() {
            // Convert leave date requests to leave date entities
            List<LeaveDate> leaveDateEntities = leaveDates.stream()
                    .map(LeaveDateRequest::toEntity)
                    .collect(Collectors.toList());
            
            // Build additional leave info
            AdditionalLeaveInfo additionalLeaveInfo = AdditionalLeaveInfo.builder()
                    .leaveType(leaveType)
                    .totalDays(BigDecimal.ZERO) // Will be calculated in service layer
                    .leaveDates(leaveDateEntities)
                    .build();
            
            // Set bidirectional relationship for leave dates
            leaveDateEntities.forEach(leaveDate -> leaveDate.setAdditionalLeaveInfo(additionalLeaveInfo));
            
            // Build and return request entity
            return org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request.builder()
                    .requestType(RequestType.LEAVE)
                    .title(title)
                    .userReason(userReason)
                    .attachmentUrl(attachmentUrl)
                    .employee(User.builder().userId(employeeId).build())
                    .additionalLeaveInfo(additionalLeaveInfo)
                    .build();
        }
    }
    
    /**
     * Request DTO for a single leave date
     */
    public record LeaveDateRequest(
            @NotNull(message = "Date is required")
            LocalDate date,
            
            @NotNull(message = "Shift type is required")
            ShiftType shiftType
    ) {
        public LeaveDate toEntity() {
            return LeaveDate.builder()
                    .date(date)
                    .shift(shiftType)
                    .build();
        }
    }
    
    /**
     * Request DTO for rejecting a request
     */
    public record RejectRequestRequest(
            @NotBlank(message = "Reject reason is required")
            String rejectReason,
            
            @NotBlank(message = "Approver ID is required")
            String approverId
    ) {}
    
    /**
     * Request DTO for approving a request
     */
    public record ApproveRequestRequest(
            @NotBlank(message = "Approver ID is required")
            String approverId
    ) {}
}
