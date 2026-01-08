package org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao.jpa_repository;

import java.util.List;

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
                        AND (:#{#filter.roles} IS NULL OR u.role IN :#{#filter.roles})
                        AND (:#{#filter.status} IS NULL OR u.status = :#{#filter.status})
                        AND (:#{#filter.departmentId} IS NULL OR u.department.departmentId = :#{#filter.departmentId})
                        AND (:#{#filter.position} IS NULL OR u.position = :#{#filter.position})
                        AND (:#{#filter.gender} IS NULL OR u.gender = :#{#filter.gender})
                        AND (:#{#filter.departmentName} IS NULL OR u.department.departmentName = :#{#filter.departmentName})
                        """)
        public Page<User> getUsers(Pageable pageable, ProfileFilter filter);

        @Query(value = """
                        SELECT u FROM User u
                        WHERE (:#{#filter.nameTerm} IS NULL OR u.fullName LIKE %:#{#filter.nameTerm}%)
                        AND (:#{#filter.roles} IS NULL OR u.role IN :#{#filter.roles})
                        AND (:#{#filter.status} IS NULL OR u.status = :#{#filter.status})
                        AND (:#{#filter.departmentId} IS NULL OR u.department.departmentId = :#{#filter.departmentId})
                        AND (:#{#filter.position} IS NULL OR u.position = :#{#filter.position})
                        AND (:#{#filter.gender} IS NULL OR u.gender = :#{#filter.gender})
                        """)
        public List<User> getAllUsers(ProfileFilter filter);

        /**
         * Find user by exact email match (case-insensitive)
         * 
         * @param email the email to search for
         * @return Optional containing User if found
         */
        java.util.Optional<User> findByEmailIgnoreCase(String email);
}
