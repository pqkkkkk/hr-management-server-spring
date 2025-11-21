package org.pqkkkkk.hr_management_server.modules.profile.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Department;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.UserRole;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileService;
import jakarta.persistence.EntityManager;
import org.pqkkkkk.hr_management_server.modules.profile.infrastructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ProfileServiceFullIntegrationTest {

    @Autowired
    ProfileService profileService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager entityManager;

    Department eng;

    @BeforeEach
    @Transactional
    void setUp() {
        // remove all departments via JPQL so we don't depend on DepartmentRepository
        entityManager.createQuery("DELETE FROM Department").executeUpdate();

        eng = Department.builder()
            .departmentName("Engineering")
            .build();
        entityManager.persist(eng);
        entityManager.flush();

        User u1 = User.builder()
                .fullName("Alice Dev")
                .email("alice.dev@example.com")
                .role(UserRole.EMPLOYEE)
                .position("Developer")
                .joinDate(LocalDate.of(2023, 1, 1))
                .createdAt(LocalDateTime.now())
                .department(eng)
                .build();

        User u2 = User.builder()
                .fullName("Bob Manager")
                .email("bob.manager@example.com")
                .role(UserRole.MANAGER)
                .position("Manager")
                .joinDate(LocalDate.of(2022, 6, 1))
                .createdAt(LocalDateTime.now())
                .department(eng)
                .build();

        userRepository.saveAll(List.of(u1, u2));
    }

    @Test
    void getAllUsers_returnsAll() {
        List<User> users = profileService.getAllUsers();
        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void getUsers_withPagingAndFilter() {
        var page = profileService.getUsers(1, 1, "fullName", "asc", "EMPLOYEE", eng.getDepartmentId());
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(page.getContent().get(0).getRole()).isEqualTo(UserRole.EMPLOYEE);
    }
}
