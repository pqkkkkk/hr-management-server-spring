package org.pqkkkkk.hr_management_server.modules.profile.domain.repositories;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffRepository extends JpaRepository<Staff, String> {
}
