package org.pqkkkkk.hr_management_server.modules.request.controller.http.dto;

import java.time.LocalDateTime;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LeaveRequestDTO {
    String requestId;
    RequestType requestType;
    RequestStatus status;
    String title;
    String userReason;
    String rejectReason;
    String attachmentUrl;
    LocalDateTime processedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static LeaveRequestDTO fromEntity(Request request) {
        return LeaveRequestDTO.builder()
            .requestId(request.getRequestId())
            .requestType(request.getRequestType())
            .status(request.getStatus())
            .title(request.getTitle())
            .userReason(request.getUserReason())
            .rejectReason(request.getRejectReason())
            .attachmentUrl(request.getAttachmentUrl())
            .processedAt(request.getProcessedAt())
            .createdAt(request.getCreatedAt())
            .updatedAt(request.getUpdatedAt())
            .build();
    }
}