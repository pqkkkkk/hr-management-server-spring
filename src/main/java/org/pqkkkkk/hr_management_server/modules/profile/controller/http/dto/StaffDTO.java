package org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StaffDTO {
    private String name;
    private String dateOfBirth;
    private String phoneNumber;
    private String email;
    private String departmentId;
    private String departmentName;
    private String position;
}