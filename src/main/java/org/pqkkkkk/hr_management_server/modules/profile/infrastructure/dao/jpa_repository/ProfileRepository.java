package org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao.jpa_repository;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.filter.FilterCriteria.ProfileFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<User, String> {

    @Query(value = """
            SELECT u FROM User u
            WHERE (:#{#filter.nameTerm} IS NULL OR u.fullName LIKE %:#{#filter.nameTerm}%)
            AND (:#{#filter.role} IS NULL OR u.role = :#{#filter.role})
            AND (:#{#filter.status} IS NULL OR u.status = :#{#filter.status})
            AND (:#{#filter.departmentId} IS NULL OR u.department.departmentId = :#{#filter.departmentId})
            AND (:#{#filter.position} IS NULL OR u.position = :#{#filter.position})
            AND (:#{#filter.gender} IS NULL OR u.gender = :#{#filter.gender})
            """)
    public Page<User> getUsers(Pageable pageable, ProfileFilter filter);
}
