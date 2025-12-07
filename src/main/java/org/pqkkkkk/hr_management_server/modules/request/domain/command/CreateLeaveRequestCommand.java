package org.pqkkkkk.hr_management_server.modules.request.domain.command;

import java.time.LocalDate;
import java.util.List;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.ShiftType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeaveRequestCommand {
    private String employeeId;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private List<ShiftType> shifts; // Optional: for half-day leaves
    private String attachmentUrl;
}
