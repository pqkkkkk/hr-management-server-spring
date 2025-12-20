package org.pqkkkkk.hr_management_server.modules.request.controller.http.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.AdditionalCheckInInfo;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.AdditionalCheckOutInfo;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.AdditionalLeaveInfo;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.LeaveType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.ShiftType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.LeaveDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
    
    /**
     * Request DTO for creating a check-in request
     */
    public record CreateCheckInRequestRequest(
            String title,
            
            String userReason,
            
            @NotBlank(message = "Employee ID is required")
            String employeeId,
            
            @NotNull(message = "Desired check-in time is required")
            LocalDateTime desiredCheckInTime
    ) {
        public org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request toEntity() {
            // Build additional check-in info
            AdditionalCheckInInfo additionalCheckInInfo = AdditionalCheckInInfo.builder()
                    .desiredCheckInTime(desiredCheckInTime)
                    .build();
            
            // Build and return request entity
            return org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request.builder()
                    .requestType(RequestType.CHECK_IN)
                    .title(title)
                    .userReason(userReason)
                    .employee(User.builder().userId(employeeId).build())
                    .additionalCheckInInfo(additionalCheckInInfo)
                    .build();
        }
    }
    
    /**
     * Request DTO for creating a check-out request
     */
    public record CreateCheckOutRequestRequest(
            String title,
            
            String userReason,
            
            @NotBlank(message = "Employee ID is required")
            String employeeId,
            
            @NotNull(message = "Desired check-out time is required")
            LocalDateTime desiredCheckOutTime
    ) {
        public org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request toEntity() {
            // Build additional check-out info
            AdditionalCheckOutInfo additionalCheckOutInfo = AdditionalCheckOutInfo.builder()
                    .desiredCheckOutTime(desiredCheckOutTime)
                    .build();
            
            // Build and return request entity
            return org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request.builder()
                    .requestType(RequestType.CHECK_OUT)
                    .title(title)
                    .userReason(userReason)
                    .employee(User.builder().userId(employeeId).build())
                    .additionalCheckOutInfo(additionalCheckOutInfo)
                    .build();
        }
    }
    
    /**
     * Request DTO for creating a timesheet update request
     */
    public record CreateTimesheetUpdateRequestRequest(
            @NotNull(message = "Work date is required")
            LocalDate workDate,

            LocalDateTime requestedCheckIn,

            LocalDateTime requestedCheckOut,

            @NotBlank(message = "Reason is required")
            @Size(min = 10, max = 500, message = "Reason must be 10-500 characters")
            String reason,

            @NotBlank(message = "Employee ID is required")
            String employeeId
    ) {
        public org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request toEntity() {
            // Build AdditionalTimesheetInfo
            org.pqkkkkk.hr_management_server.modules.request.domain.entity.AdditionalTimesheetInfo additionalTimesheetInfo = org.pqkkkkk.hr_management_server.modules.request.domain.entity.AdditionalTimesheetInfo.builder()
                .targetDate(workDate)
                .desiredCheckInTime(requestedCheckIn)
                .desiredCheckOutTime(requestedCheckOut)
                .build();

                // Build Employee entity
            org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User employee = org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User.builder()
                .userId(employeeId)
                .build();

            // Build Request entity
            org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request request = org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request.builder()
                .requestType(org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType.TIMESHEET)
                .userReason(reason)
                .employee(employee)
                .additionalTimesheetInfo(additionalTimesheetInfo)
                .build();

            // Set bidirectional relationship
            additionalTimesheetInfo.setRequest(request);
            return request;
        }
    }
}
