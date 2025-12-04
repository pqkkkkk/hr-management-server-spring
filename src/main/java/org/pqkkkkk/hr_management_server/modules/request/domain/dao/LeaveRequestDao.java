package org.pqkkkkk.hr_management_server.modules.request.domain.dao;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestDao {
    List<String> findOverlappingRequestIds(String employeeId, LocalDate startDate, LocalDate endDate);
}
