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
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.AdditionalTimesheetInfo;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.AdditionalWfhInfo;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.LeaveType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.ShiftType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.LeaveDate;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.WfhDate;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.Enums.AttendanceStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTOs for Request module (Leave Requests).
 * <p>
 * They are used to encapsulate data sent by clients in HTTP requests.
 * <p>
 * Each request DTO includes methods to convert to entity objects.
 */
public class Request {

        /**
         * Request DTO for creating a leave request
         */
        public record CreateLeaveRequestRequest(
                        @NotBlank(message = "Title is required") String title,

                        @NotBlank(message = "User reason is required") String userReason,

                        String attachmentUrl,

                        @NotBlank(message = "Employee ID is required") String employeeId,

                        @NotNull(message = "Leave type is required") LeaveType leaveType,

                        @NotEmpty(message = "Leave dates cannot be empty") @Valid List<LeaveDateRequest> leaveDates) {
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
                        @NotNull(message = "Date is required") LocalDate date,

                        @NotNull(message = "Shift type is required") ShiftType shiftType) {
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
                        @NotBlank(message = "Rejecter ID is required") @NotNull(message = "Rejecter ID cannot be null") String rejecterId,
                        @NotBlank(message = "Reject reason is required") String rejectReason) {
        }

        /**
         * Request DTO for approving a request
         */
        public record ApproveRequestRequest(
                        @NotBlank(message = "Approver ID is required") @NotNull(message = "Approver ID cannot be null") String approverId) {
        }

        /**
         * Request DTO for delegating a request to another processor
         */
        public record DelegateRequestRequest(
                        @NotBlank(message = "New processor ID is required") @NotNull(message = "New processor ID cannot be null") String newProcessorId) {
        }

        /**
         * Request DTO for creating a check-in request
         */
        public record CreateCheckInRequestRequest(
                        String title,

                        String userReason,

                        String attachmentUrl,

                        @NotBlank(message = "Employee ID is required") String employeeId,

                        @NotNull(message = "Desired check-in time is required") LocalDateTime desiredCheckInTime) {
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
                                        .attachmentUrl(attachmentUrl)
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

                        String attachmentUrl,

                        @NotBlank(message = "Employee ID is required") String employeeId,

                        @NotNull(message = "Desired check-out time is required") LocalDateTime desiredCheckOutTime) {
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
                                        .attachmentUrl(attachmentUrl)
                                        .employee(User.builder().userId(employeeId).build())
                                        .additionalCheckOutInfo(additionalCheckOutInfo)
                                        .build();
                }
        }

        /**
         * Request DTO for creating a timesheet correction request
         */
        public record CreateTimeSheetRequestRequest(
                        String title,

                        String userReason,

                        String attachmentUrl,

                        @NotBlank(message = "Employee ID is required") String employeeId,

                        @NotNull(message = "Target date is required") LocalDate targetDate,

                        @NotNull(message = "Desired check-in time is required") LocalDateTime desiredCheckInTime,

                        @NotNull(message = "Current check-in time is required") LocalDateTime currentCheckInTime,

                        @NotNull(message = "Desired check-out time is required") LocalDateTime desiredCheckOutTime,

                        @NotNull(message = "Current check-out time is required") LocalDateTime currentCheckOutTime) {
                public org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request toEntity() {
                        // Build additional timesheet info
                        AdditionalTimesheetInfo additionalTimesheetInfo = AdditionalTimesheetInfo.builder()
                                        .targetDate(targetDate)
                                        .desiredCheckInTime(desiredCheckInTime)
                                        .currentCheckInTime(currentCheckInTime)
                                        .desiredCheckOutTime(desiredCheckOutTime)
                                        .currentCheckOutTime(currentCheckOutTime)
                                        .build();

                        // Build and return request entity
                        return org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request.builder()
                                        .requestType(RequestType.TIMESHEET)
                                        .title(title)
                                        .userReason(userReason)
                                        .attachmentUrl(attachmentUrl)
                                        .employee(User.builder().userId(employeeId).build())
                                        .additionalTimesheetInfo(additionalTimesheetInfo)
                                        .build();
                }
        }

        /**
         * Request DTO for creating an extended timesheet correction request (V2).
         * Supports updating attendance status and WFH flags in addition to check times.
         */
        public record CreateTimeSheetRequestRequestV2(
                        String title,

                        String userReason,

                        String attachmentUrl,

                        @NotBlank(message = "Employee ID is required") String employeeId,

                        @NotNull(message = "Target date is required") LocalDate targetDate,

                        // Check times - now optional for status-only updates
                        LocalDateTime desiredCheckInTime,

                        LocalDateTime currentCheckInTime,

                        LocalDateTime desiredCheckOutTime,

                        LocalDateTime currentCheckOutTime,

                        // New extended fields for status updates
                        AttendanceStatus desiredMorningStatus, // PRESENT, LEAVE, or null for no change

                        AttendanceStatus desiredAfternoonStatus, // PRESENT, LEAVE, or null for no change

                        Boolean desiredMorningWfh, // true, false, or null for no change

                        Boolean desiredAfternoonWfh // true, false, or null for no change
        ) {
                public org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request toEntity() {
                        // Build additional timesheet info with extended fields
                        AdditionalTimesheetInfo additionalTimesheetInfo = AdditionalTimesheetInfo.builder()
                                        .targetDate(targetDate)
                                        .desiredCheckInTime(desiredCheckInTime)
                                        .currentCheckInTime(currentCheckInTime)
                                        .desiredCheckOutTime(desiredCheckOutTime)
                                        .currentCheckOutTime(currentCheckOutTime)
                                        .desiredMorningStatus(desiredMorningStatus)
                                        .desiredAfternoonStatus(desiredAfternoonStatus)
                                        .desiredMorningWfh(desiredMorningWfh)
                                        .desiredAfternoonWfh(desiredAfternoonWfh)
                                        .build();

                        // Build and return request entity
                        return org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request.builder()
                                        .requestType(RequestType.TIMESHEET)
                                        .title(title)
                                        .userReason(userReason)
                                        .attachmentUrl(attachmentUrl)
                                        .employee(User.builder().userId(employeeId).build())
                                        .additionalTimesheetInfo(additionalTimesheetInfo)
                                        .build();
                }
        }

        /**
         * Request DTO for a single WFH date
         */
        public record WfhDateRequest(
                        @NotNull(message = "Date is required") LocalDate date,

                        @NotNull(message = "Shift type is required") ShiftType shiftType) {
                public WfhDate toEntity() {
                        return WfhDate.builder()
                                        .date(date)
                                        .shift(shiftType)
                                        .build();
                }
        }

        /**
         * Request DTO for creating a work from home request
         */
        public record CreateWfhRequestRequest(
                        String title,

                        @NotBlank(message = "User reason is required") String userReason,

                        String attachmentUrl,

                        @NotBlank(message = "Employee ID is required") String employeeId,

                        Boolean wfhCommitment,

                        String workLocation,

                        @NotEmpty(message = "WFH dates cannot be empty") @Valid List<WfhDateRequest> wfhDates) {
                public org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request toEntity() {
                        // Convert wfh date requests to wfh date entities
                        List<WfhDate> wfhDateEntities = wfhDates.stream()
                                        .map(WfhDateRequest::toEntity)
                                        .collect(Collectors.toList());

                        // Build additional wfh info
                        AdditionalWfhInfo additionalWfhInfo = AdditionalWfhInfo.builder()
                                        .wfhCommitment(wfhCommitment != null ? wfhCommitment : false)
                                        .workLocation(workLocation)
                                        .totalDays(BigDecimal.ZERO) // Will be calculated in service layer
                                        .wfhDates(wfhDateEntities)
                                        .build();

                        // Build and return request entity
                        return org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request.builder()
                                        .requestType(RequestType.WFH)
                                        .title(title)
                                        .userReason(userReason)
                                        .attachmentUrl(attachmentUrl)
                                        .employee(User.builder().userId(employeeId).build())
                                        .additionalWfhInfo(additionalWfhInfo)
                                        .build();
                }
        }
}
