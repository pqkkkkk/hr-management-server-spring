package org.pqkkkkk.hr_management_server.modules.request.controller.http.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.LeaveType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.ShiftType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.LeaveDate;

/**
 * Data Transfer Objects for Request module (Leave Requests).
 * <p>
 * Each DTO includes methods to convert to and from entity objects
 */
public class DTO {

    /**
     * General DTO for all request types
     * This DTO includes common fields for all requests and type-specific additional
     * info
     */
    public record RequestDTO(
            String requestId,
            RequestType requestType,
            RequestStatus status,
            String title,
            String userReason,
            String rejectReason,
            String attachmentUrl,
            String employeeId,
            String employeeFullName,
            String employeeDepartmentId,
            String employeeDepartmentName,
            String approverId,
            String approverFullName,
            String processorId,
            String processorFullName,
            LocalDateTime processedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        public static RequestDTO fromEntity(
                org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request request) {
            if (request == null) {
                return null;
            }

            return new RequestDTO(
                    request.getRequestId(),
                    request.getRequestType(),
                    request.getStatus(),
                    request.getTitle(),
                    request.getUserReason(),
                    request.getRejectReason(),
                    request.getAttachmentUrl(),
                    request.getEmployee() != null ? request.getEmployee().getUserId() : null,
                    request.getEmployee() != null ? request.getEmployee().getFullName() : null,
                    request.getEmployee() != null && request.getEmployee().getDepartment() != null
                            ? request.getEmployee().getDepartment().getDepartmentId()
                            : null,
                    request.getEmployee() != null && request.getEmployee().getDepartment() != null
                            ? request.getEmployee().getDepartment().getDepartmentName()
                            : null,
                    request.getApprover() != null ? request.getApprover().getUserId() : null,
                    request.getApprover() != null ? request.getApprover().getFullName() : null,
                    request.getProcessor() != null ? request.getProcessor().getUserId() : null,
                    request.getProcessor() != null ? request.getProcessor().getFullName() : null,
                    request.getProcessedAt(),
                    request.getCreatedAt(),
                    request.getUpdatedAt());
        }
    }

    /**
     * DTO for leave request response
     */
    public record LeaveRequestDTO(
            String requestId,
            RequestType requestType,
            RequestStatus status,
            String title,
            String userReason,
            String rejectReason,
            String attachmentUrl,
            String employeeId,
            String employeeFullName,
            String employeeDepartmentId,
            String employeeDepartmentName,
            String approverId,
            String approverFullName,
            String processorId,
            String processorFullName,
            LocalDateTime processedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LeaveType leaveType,
            BigDecimal totalDays,
            List<LeaveDateDTO> leaveDates) {
        public static LeaveRequestDTO fromEntity(
                org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request request) {
            if (request == null) {
                return null;
            }

            // Convert leave dates to DTOs
            List<LeaveDateDTO> leaveDateDTOs = null;
            LeaveType leaveType = null;
            BigDecimal totalDays = null;

            if (request.getAdditionalLeaveInfo() != null) {
                leaveType = request.getAdditionalLeaveInfo().getLeaveType();
                totalDays = request.getAdditionalLeaveInfo().getTotalDays();

                if (request.getAdditionalLeaveInfo().getLeaveDates() != null) {
                    leaveDateDTOs = request.getAdditionalLeaveInfo().getLeaveDates().stream()
                            .map(LeaveDateDTO::fromEntity)
                            .collect(Collectors.toList());
                }
            }

            return new LeaveRequestDTO(
                    request.getRequestId(),
                    request.getRequestType(),
                    request.getStatus(),
                    request.getTitle(),
                    request.getUserReason(),
                    request.getRejectReason(),
                    request.getAttachmentUrl(),
                    request.getEmployee() != null ? request.getEmployee().getUserId() : null,
                    request.getEmployee() != null ? request.getEmployee().getFullName() : null,
                    request.getEmployee() != null && request.getEmployee().getDepartment() != null
                            ? request.getEmployee().getDepartment().getDepartmentId()
                            : null,
                    request.getEmployee() != null && request.getEmployee().getDepartment() != null
                            ? request.getEmployee().getDepartment().getDepartmentName()
                            : null,
                    request.getApprover() != null ? request.getApprover().getUserId() : null,
                    request.getApprover() != null ? request.getApprover().getFullName() : null,
                    request.getProcessor() != null ? request.getProcessor().getUserId() : null,
                    request.getProcessor() != null ? request.getProcessor().getFullName() : null,
                    request.getProcessedAt(),
                    request.getCreatedAt(),
                    request.getUpdatedAt(),
                    leaveType,
                    totalDays,
                    leaveDateDTOs);
        }
    }

    /**
     * DTO for a single leave date
     */
    public record LeaveDateDTO(
            String leaveDateId,
            LocalDate date,
            ShiftType shiftType) {
        public static LeaveDateDTO fromEntity(LeaveDate leaveDate) {
            if (leaveDate == null) {
                return null;
            }

            return new LeaveDateDTO(
                    leaveDate.getLeaveDateId(),
                    leaveDate.getDate(),
                    leaveDate.getShift());
        }
    }

    /**
     * DTO for check-in request response
     */
    public record CheckInRequestDTO(
            String requestId,
            String attachmentUrl,
            RequestType requestType,
            RequestStatus status,
            String title,
            String userReason,
            String rejectReason,
            String employeeId,
            String employeeFullName,
            String employeeDepartmentId,
            String employeeDepartmentName,
            String approverId,
            String approverFullName,
            String processorId,
            String processorFullName,
            LocalDateTime processedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime desiredCheckInTime) {
        public static CheckInRequestDTO fromEntity(
                org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request request) {
            if (request == null) {
                return null;
            }

            LocalDateTime desiredCheckInTime = null;

            if (request.getAdditionalCheckInInfo() != null) {
                desiredCheckInTime = request.getAdditionalCheckInInfo().getDesiredCheckInTime();
            }

            return new CheckInRequestDTO(
                    request.getRequestId(),
                    request.getAttachmentUrl(),
                    request.getRequestType(),
                    request.getStatus(),
                    request.getTitle(),
                    request.getUserReason(),
                    request.getRejectReason(),
                    request.getEmployee() != null ? request.getEmployee().getUserId() : null,
                    request.getEmployee() != null ? request.getEmployee().getFullName() : null,
                    request.getEmployee() != null && request.getEmployee().getDepartment() != null
                            ? request.getEmployee().getDepartment().getDepartmentId()
                            : null,
                    request.getEmployee() != null && request.getEmployee().getDepartment() != null
                            ? request.getEmployee().getDepartment().getDepartmentName()
                            : null,
                    request.getApprover() != null ? request.getApprover().getUserId() : null,
                    request.getApprover() != null ? request.getApprover().getFullName() : null,
                    request.getProcessor() != null ? request.getProcessor().getUserId() : null,
                    request.getProcessor() != null ? request.getProcessor().getFullName() : null,
                    request.getProcessedAt(),
                    request.getCreatedAt(),
                    request.getUpdatedAt(),
                    desiredCheckInTime);
        }
    }

    /**
     * DTO for check-out request response
     */
    public record CheckOutRequestDTO(
            String requestId,
            String attachmentUrl,
            RequestType requestType,
            RequestStatus status,
            String title,
            String userReason,
            String rejectReason,
            String employeeId,
            String employeeFullName,
            String employeeDepartmentId,
            String employeeDepartmentName,
            String approverId,
            String approverFullName,
            String processorId,
            String processorFullName,
            LocalDateTime processedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime desiredCheckOutTime) {
        public static CheckOutRequestDTO fromEntity(
                org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request request) {
            if (request == null) {
                return null;
            }

            LocalDateTime desiredCheckOutTime = null;

            if (request.getAdditionalCheckOutInfo() != null) {
                desiredCheckOutTime = request.getAdditionalCheckOutInfo().getDesiredCheckOutTime();
            }

            return new CheckOutRequestDTO(
                    request.getRequestId(),
                    request.getAttachmentUrl(),
                    request.getRequestType(),
                    request.getStatus(),
                    request.getTitle(),
                    request.getUserReason(),
                    request.getRejectReason(),
                    request.getEmployee() != null ? request.getEmployee().getUserId() : null,
                    request.getEmployee() != null ? request.getEmployee().getFullName() : null,
                    request.getEmployee() != null && request.getEmployee().getDepartment() != null
                            ? request.getEmployee().getDepartment().getDepartmentId()
                            : null,
                    request.getEmployee() != null && request.getEmployee().getDepartment() != null
                            ? request.getEmployee().getDepartment().getDepartmentName()
                            : null,
                    request.getApprover() != null ? request.getApprover().getUserId() : null,
                    request.getApprover() != null ? request.getApprover().getFullName() : null,
                    request.getProcessor() != null ? request.getProcessor().getUserId() : null,
                    request.getProcessor() != null ? request.getProcessor().getFullName() : null,
                    request.getProcessedAt(),
                    request.getCreatedAt(),
                    request.getUpdatedAt(),
                    desiredCheckOutTime);
        }
    }

    /**
     * DTO for timesheet request response
     */
    public record TimeSheetRequestDTO(
            String requestId,
            String attachmentUrl,
            RequestType requestType,
            RequestStatus status,
            String title,
            String userReason,
            String rejectReason,
            String employeeId,
            String employeeFullName,
            String employeeDepartmentId,
            String employeeDepartmentName,
            String approverId,
            String approverFullName,
            String processorId,
            String processorFullName,
            LocalDateTime processedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDate targetDate,
            LocalDateTime desiredCheckInTime,
            LocalDateTime currentCheckInTime,
            LocalDateTime desiredCheckOutTime,
            LocalDateTime currentCheckOutTime) {
        public static TimeSheetRequestDTO fromEntity(
                org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request request) {
            if (request == null) {
                return null;
            }

            LocalDate targetDate = null;
            LocalDateTime desiredCheckInTime = null;
            LocalDateTime currentCheckInTime = null;
            LocalDateTime desiredCheckOutTime = null;
            LocalDateTime currentCheckOutTime = null;

            if (request.getAdditionalTimesheetInfo() != null) {
                targetDate = request.getAdditionalTimesheetInfo().getTargetDate();
                desiredCheckInTime = request.getAdditionalTimesheetInfo().getDesiredCheckInTime();
                currentCheckInTime = request.getAdditionalTimesheetInfo().getCurrentCheckInTime();
                desiredCheckOutTime = request.getAdditionalTimesheetInfo().getDesiredCheckOutTime();
                currentCheckOutTime = request.getAdditionalTimesheetInfo().getCurrentCheckOutTime();
            }

            return new TimeSheetRequestDTO(
                    request.getRequestId(),
                    request.getAttachmentUrl(),
                    request.getRequestType(),
                    request.getStatus(),
                    request.getTitle(),
                    request.getUserReason(),
                    request.getRejectReason(),
                    request.getEmployee() != null ? request.getEmployee().getUserId() : null,
                    request.getEmployee() != null ? request.getEmployee().getFullName() : null,
                    request.getEmployee() != null && request.getEmployee().getDepartment() != null
                            ? request.getEmployee().getDepartment().getDepartmentId()
                            : null,
                    request.getEmployee() != null && request.getEmployee().getDepartment() != null
                            ? request.getEmployee().getDepartment().getDepartmentName()
                            : null,
                    request.getApprover() != null ? request.getApprover().getUserId() : null,
                    request.getApprover() != null ? request.getApprover().getFullName() : null,
                    request.getProcessor() != null ? request.getProcessor().getUserId() : null,
                    request.getProcessor() != null ? request.getProcessor().getFullName() : null,
                    request.getProcessedAt(),
                    request.getCreatedAt(),
                    request.getUpdatedAt(),
                    targetDate,
                    desiredCheckInTime,
                    currentCheckInTime,
                    desiredCheckOutTime,
                    currentCheckOutTime);
        }
    }

    /**
     * DTO for a single WFH date
     */
    public record WfhDateDTO(
            String wfhDateId,
            LocalDate date,
            ShiftType shiftType) {
        public static WfhDateDTO fromEntity(
                org.pqkkkkk.hr_management_server.modules.request.domain.entity.WfhDate wfhDate) {
            if (wfhDate == null) {
                return null;
            }

            return new WfhDateDTO(
                    wfhDate.getWfhDateId(),
                    wfhDate.getDate(),
                    wfhDate.getShift());
        }
    }

    /**
     * DTO for WFH (work from home) request response
     */
    public record WfhRequestDTO(
            String requestId,
            RequestType requestType,
            RequestStatus status,
            String title,
            String userReason,
            String rejectReason,
            String attachmentUrl,
            String employeeId,
            String employeeFullName,
            String employeeDepartmentId,
            String employeeDepartmentName,
            String approverId,
            String approverFullName,
            String processorId,
            String processorFullName,
            LocalDateTime processedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean wfhCommitment,
            String workLocation,
            BigDecimal totalDays,
            List<WfhDateDTO> wfhDates) {
        public static WfhRequestDTO fromEntity(
                org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request request) {
            if (request == null) {
                return null;
            }

            // Convert wfh dates to DTOs
            List<WfhDateDTO> wfhDateDTOs = null;
            Boolean wfhCommitment = null;
            String workLocation = null;
            BigDecimal totalDays = null;

            if (request.getAdditionalWfhInfo() != null) {
                wfhCommitment = request.getAdditionalWfhInfo().getWfhCommitment();
                workLocation = request.getAdditionalWfhInfo().getWorkLocation();
                totalDays = request.getAdditionalWfhInfo().getTotalDays();

                if (request.getAdditionalWfhInfo().getWfhDates() != null) {
                    wfhDateDTOs = request.getAdditionalWfhInfo().getWfhDates().stream()
                            .map(WfhDateDTO::fromEntity)
                            .collect(Collectors.toList());
                }
            }

            return new WfhRequestDTO(
                    request.getRequestId(),
                    request.getRequestType(),
                    request.getStatus(),
                    request.getTitle(),
                    request.getUserReason(),
                    request.getRejectReason(),
                    request.getAttachmentUrl(),
                    request.getEmployee() != null ? request.getEmployee().getUserId() : null,
                    request.getEmployee() != null ? request.getEmployee().getFullName() : null,
                    request.getEmployee() != null && request.getEmployee().getDepartment() != null
                            ? request.getEmployee().getDepartment().getDepartmentId()
                            : null,
                    request.getEmployee() != null && request.getEmployee().getDepartment() != null
                            ? request.getEmployee().getDepartment().getDepartmentName()
                            : null,
                    request.getApprover() != null ? request.getApprover().getUserId() : null,
                    request.getApprover() != null ? request.getApprover().getFullName() : null,
                    request.getProcessor() != null ? request.getProcessor().getUserId() : null,
                    request.getProcessor() != null ? request.getProcessor().getFullName() : null,
                    request.getProcessedAt(),
                    request.getCreatedAt(),
                    request.getUpdatedAt(),
                    wfhCommitment,
                    workLocation,
                    totalDays,
                    wfhDateDTOs);
        }
    }
}
