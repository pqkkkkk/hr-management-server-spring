package org.pqkkkkk.hr_management_server.modules.profile.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Department;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.UserRole;
import org.pqkkkkk.hr_management_server.modules.profile.infrastructure.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ProfileServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Department department;

    @BeforeEach
    void setUp() {
        // Reset database trước mỗi test (nếu cần)
        userRepository.deleteAll();

        // Tạo department và persist trước bằng TestEntityManager
        department = Department.builder()
            .departmentName("IT Department")
            .build();
        entityManager.persist(department);
        entityManager.flush();
    }

    @Test
    void testSaveAndRetrieveUser() {
        // Tạo user và gắn department
        User user = User.builder()
            .fullName("Nguyen Van A")
            .email("a@example.com")
            .role(UserRole.EMPLOYEE)
            .position("Developer")
            .joinDate(LocalDate.now())
            .identityCardNumber("123456789")
            .phoneNumber("0987654321")
            .dateOfBirth(LocalDate.of(1990,1,1))
            .address("Hanoi")
            .bankAccountNumber("111222333")
            .bankName("Vietcombank")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .department(department)
            .build();

        // Persist user
        userRepository.save(user);

        // Lấy lại user từ DB
        var all = userRepository.findAll();
        assertThat(all).hasSizeGreaterThanOrEqualTo(1);
        User retrieved = all.get(0);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getFullName()).isEqualTo("Nguyen Van A");
        assertThat(retrieved.getDepartment()).isNotNull();
        assertThat(retrieved.getDepartment().getDepartmentName()).isEqualTo("IT Department");
    }
}
