package org.pqkkkkk.hr_management_server.modules.request.domain.command;

import java.time.LocalDate;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestFilterCommand {
    private String employeeId;
    private RequestStatus status;
    private RequestType requestType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer page;
    private Integer size;
    
    @Builder.Default
    private String sortBy = "createdAt";
    
    @Builder.Default
    private String sortDirection = "DESC";
}
