package org.pqkkkkk.hr_management_server.modules.request.infrastructure.dao;

import java.time.LocalDate;
import java.util.List;

import org.pqkkkkk.hr_management_server.modules.request.domain.dao.LeaveRequestDao;
import org.pqkkkkk.hr_management_server.modules.request.infrastructure.dao.jpa_repository.LeaveDateRepository;
import org.springframework.stereotype.Component;

@Component
public class JpaLeaveRequestDao implements LeaveRequestDao {

    private final LeaveDateRepository leaveDateRepository;

    public JpaLeaveRequestDao(LeaveDateRepository leaveDateRepository) {
        this.leaveDateRepository = leaveDateRepository;
    }

    @Override
    public List<String> findOverlappingRequestIds(String employeeId, LocalDate startDate, LocalDate endDate) {
        return leaveDateRepository.findOverlappingRequestIds(employeeId, startDate, endDate);
    }
}
