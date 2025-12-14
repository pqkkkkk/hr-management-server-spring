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
 * <p> Each DTO includes methods to convert to and from entity objects
 */
public class DTO {
        /**
         * DTO for check-out request response
         */
        public record CheckOutRequestDTO(
                String requestId,
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
                java.time.LocalDateTime processedAt,
                java.time.LocalDateTime createdAt,
                java.time.LocalDateTime updatedAt,
                java.time.LocalDateTime desiredCheckOutTime,
                java.time.LocalDateTime currentCheckOutTime,
                String attachmentUrl
        ) {
            public static CheckOutRequestDTO fromEntity(org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request request) {
                java.time.LocalDateTime desiredCheckOutTime = null;
                java.time.LocalDateTime currentCheckOutTime = null;
                if (request.getAdditionalCheckOutInfo() != null) {
                    desiredCheckOutTime = request.getAdditionalCheckOutInfo().getDesiredCheckOutTime();
                    currentCheckOutTime = request.getAdditionalCheckOutInfo().getCurrentCheckOutTime();
                }
                return new CheckOutRequestDTO(
                        request.getRequestId(),
                        request.getRequestType(),
                        request.getStatus(),
                        request.getTitle(),
                        request.getUserReason(),
                        request.getRejectReason(),
                        request.getEmployee() != null ? request.getEmployee().getUserId() : null,
                        request.getEmployee() != null ? request.getEmployee().getFullName() : null,
                        request.getEmployee() != null && request.getEmployee().getDepartment() != null ? request.getEmployee().getDepartment().getDepartmentId() : null,
                        request.getEmployee() != null && request.getEmployee().getDepartment() != null ? request.getEmployee().getDepartment().getDepartmentName() : null,
                        request.getApprover() != null ? request.getApprover().getUserId() : null,
                        request.getApprover() != null ? request.getApprover().getFullName() : null,
                        request.getProcessor() != null ? request.getProcessor().getUserId() : null,
                        request.getProcessor() != null ? request.getProcessor().getFullName() : null,
                        request.getProcessedAt(),
                        request.getCreatedAt(),
                        request.getUpdatedAt(),
                        desiredCheckOutTime,
                        currentCheckOutTime,
                        request.getAttachmentUrl()
                );
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
            List<LeaveDateDTO> leaveDates
    ) {
        public static LeaveRequestDTO fromEntity(org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request request) {
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
                            ? request.getEmployee().getDepartment().getDepartmentId() : null,
                    request.getEmployee() != null && request.getEmployee().getDepartment() != null 
                            ? request.getEmployee().getDepartment().getDepartmentName() : null,
                    request.getApprover() != null ? request.getApprover().getUserId() : null,
                    request.getApprover() != null ? request.getApprover().getFullName() : null,
                    request.getProcessor() != null ? request.getProcessor().getUserId() : null,
                    request.getProcessor() != null ? request.getProcessor().getFullName() : null,
                    request.getProcessedAt(),
                    request.getCreatedAt(),
                    request.getUpdatedAt(),
                    leaveType,
                    totalDays,
                    leaveDateDTOs
            );
        }
    }
    
    /**
     * DTO for a single leave date
     */
    public record LeaveDateDTO(
            String leaveDateId,
            LocalDate date,
            ShiftType shiftType
    ) {
        public static LeaveDateDTO fromEntity(LeaveDate leaveDate) {
            if (leaveDate == null) {
                return null;
            }
            
            return new LeaveDateDTO(
                    leaveDate.getLeaveDateId(),
                    leaveDate.getDate(),
                    leaveDate.getShift()
            );
        }
    }
}
