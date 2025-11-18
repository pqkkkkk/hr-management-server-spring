package org.pqkkkkk.hr_management_server.modules.profile.domain.service;

import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.StaffDTO;
import java.util.List;

public interface StaffService {
    StaffDTO createStaff(StaffDTO staffDTO);
    StaffDTO updateStaff(String id, StaffDTO staffDTO);
    StaffDTO getStaffById(String id);
    List<StaffDTO> getAllStaff();
    void deleteStaff(String id);
}
