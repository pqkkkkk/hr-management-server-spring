package org.pqkkkkk.hr_management_server.modules.profile.domain.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Department;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Staff;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Staff Repository Tests")
class StaffRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StaffRepository staffRepository;

    private Staff staff;
    private Department department;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .departmentName("Engineering")
                .build();
        entityManager.persistAndFlush(department);

        staff = Staff.builder()
                .name("John Doe")
                .dateOfBirth("1990-01-15")
                .phoneNumber("0123456789")
                .email("john.doe@example.com")
                .position("Software Engineer")
                .department(department)
                .build();
    }

    @Test
    @DisplayName("Should save Staff successfully")
    void testSaveStaff() {
        // Act
        Staff savedStaff = staffRepository.save(staff);

        // Assert
        assertNotNull(savedStaff);
        assertNotNull(savedStaff.getId());
        assertEquals("John Doe", savedStaff.getName());
        assertEquals("john.doe@example.com", savedStaff.getEmail());
    }

    @Test
    @DisplayName("Should find Staff by ID")
    void testFindStaffById() {
        // Arrange
        Staff savedStaff = staffRepository.save(staff);

        // Act
        Optional<Staff> foundStaff = staffRepository.findById(savedStaff.getId());

        // Assert
        assertTrue(foundStaff.isPresent());
        assertEquals("John Doe", foundStaff.get().getName());
        assertEquals("john.doe@example.com", foundStaff.get().getEmail());
    }

    @Test
    @DisplayName("Should return empty Optional when Staff not found")
    void testFindStaffByIdNotFound() {
        // Act
        Optional<Staff> foundStaff = staffRepository.findById("non-existent-id");

        // Assert
        assertFalse(foundStaff.isPresent());
    }

    @Test
    @DisplayName("Should find all Staff")
    void testFindAllStaff() {
        // Arrange
        Staff staff1 = Staff.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();
        Staff staff2 = Staff.builder()
                .name("Jane Smith")
                .email("jane@example.com")
                .build();

        staffRepository.save(staff1);
        staffRepository.save(staff2);

        // Act
        List<Staff> allStaff = staffRepository.findAll();

        // Assert
        assertNotNull(allStaff);
        assertEquals(2, allStaff.size());
    }

    @Test
    @DisplayName("Should update Staff successfully")
    void testUpdateStaff() {
        // Arrange
        Staff savedStaff = staffRepository.save(staff);

        // Act
        savedStaff.setName("John Updated");
        savedStaff.setPosition("Senior Engineer");
        Staff updatedStaff = staffRepository.save(savedStaff);

        // Assert
        assertEquals("John Updated", updatedStaff.getName());
        assertEquals("Senior Engineer", updatedStaff.getPosition());
    }

    @Test
    @DisplayName("Should delete Staff successfully")
    void testDeleteStaff() {
        // Arrange
        Staff savedStaff = staffRepository.save(staff);
        String staffId = savedStaff.getId();

        // Act
        staffRepository.deleteById(staffId);

        // Assert
        assertFalse(staffRepository.findById(staffId).isPresent());
    }

    @Test
    @DisplayName("Should check if Staff exists by ID")
    void testExistsById() {
        // Arrange
        Staff savedStaff = staffRepository.save(staff);

        // Act
        boolean exists = staffRepository.existsById(savedStaff.getId());

        // Assert
        assertTrue(exists);
    }

    @Test
    @DisplayName("Should return false when checking non-existent Staff")
    void testExistsByIdNotFound() {
        // Act
        boolean exists = staffRepository.existsById("non-existent-id");

        // Assert
        assertFalse(exists);
    }

    @Test
    @DisplayName("Should get count of all Staff")
    void testCountStaff() {
        // Arrange
        staffRepository.save(Staff.builder().name("Staff 1").build());
        staffRepository.save(Staff.builder().name("Staff 2").build());

        // Act
        long count = staffRepository.count();

        // Assert
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Should save Staff with null department")
    void testSaveStaffWithNullDepartment() {
        // Arrange
        Staff staffWithoutDept = Staff.builder()
                .name("Solo Staff")
                .email("solo@example.com")
                .department(null)
                .build();

        // Act
        Staff savedStaff = staffRepository.save(staffWithoutDept);

        // Assert
        assertNotNull(savedStaff);
        assertEquals("Solo Staff", savedStaff.getName());
        assertNull(savedStaff.getDepartment());
    }

    @Test
    @DisplayName("Should save multiple Staff and find all")
    void testSaveMultipleAndFindAll() {
        // Arrange
        Staff staff1 = Staff.builder()
                .name("Employee 1")
                .email("emp1@example.com")
                .position("Developer")
                .build();
        Staff staff2 = Staff.builder()
                .name("Employee 2")
                .email("emp2@example.com")
                .position("Designer")
                .build();
        Staff staff3 = Staff.builder()
                .name("Employee 3")
                .email("emp3@example.com")
                .position("Manager")
                .build();

        // Act
        staffRepository.save(staff1);
        staffRepository.save(staff2);
        staffRepository.save(staff3);
        List<Staff> allStaff = staffRepository.findAll();

        // Assert
        assertEquals(3, allStaff.size());
        assertTrue(allStaff.stream().anyMatch(s -> s.getName().equals("Employee 1")));
        assertTrue(allStaff.stream().anyMatch(s -> s.getPosition().equals("Designer")));
    }

    @Test
    @DisplayName("Should delete all Staff")
    void testDeleteAll() {
        // Arrange
        staffRepository.save(Staff.builder().name("Staff 1").build());
        staffRepository.save(Staff.builder().name("Staff 2").build());

        // Act
        staffRepository.deleteAll();

        // Assert
        assertEquals(0, staffRepository.count());
    }

    @Test
    @DisplayName("Should handle Staff with special characters")
    void testSaveStaffWithSpecialCharacters() {
        // Arrange
        Staff specialStaff = Staff.builder()
                .name("José María O'Connor")
                .email("jose.maria+special@example.com")
                .phoneNumber("+1-800-555-0123")
                .position("Développeur")
                .build();

        // Act
        Staff savedStaff = staffRepository.save(specialStaff);

        // Assert
        assertNotNull(savedStaff);
        assertEquals("José María O'Connor", savedStaff.getName());
        assertEquals("jose.maria+special@example.com", savedStaff.getEmail());
    }
}

