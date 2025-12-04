package org.pqkkkkk.hr_management_server.modules.request.infrastructure.dao.jpa_repository;

import java.time.LocalDate;
import java.util.List;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.LeaveDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveDateRepository extends JpaRepository<LeaveDate, String> {
    
    @Query("""
        SELECT DISTINCT ld.additionalLeaveInfo.requestId 
        FROM LeaveDate ld 
        WHERE ld.additionalLeaveInfo.request.employee.userId = :employeeId 
        AND ld.date BETWEEN :startDate AND :endDate
        AND ld.additionalLeaveInfo.request.status IN ('PENDING', 'APPROVED')
        """)
    List<String> findOverlappingRequestIds(
        @Param("employeeId") String employeeId, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );
}
