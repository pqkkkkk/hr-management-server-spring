
package org.pqkkkkk.hr_management_server.modules.request.controller.http.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

import org.pqkkkkk.hr_management_server.modules.request.domain.command.CreateLeaveRequestCommand;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.ShiftType;
import lombok.Data;

@Data
public class CreateLeaveRequestBody {
    @NotBlank
    private String leaveType;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    private String reason;
    private List<ShiftType> shifts;
    private String attachmentUrl;
    
    public CreateLeaveRequestCommand toCommand(String employeeId) {
        return CreateLeaveRequestCommand.builder()
            .employeeId(employeeId)
            .leaveType(leaveType)
            .startDate(startDate)
            .endDate(endDate)
            .reason(reason)
            .shifts(shifts)
            .attachmentUrl(attachmentUrl)
            .build();
    }
}