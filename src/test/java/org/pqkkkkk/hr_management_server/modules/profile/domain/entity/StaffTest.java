package org.pqkkkkk.hr_management_server.modules.profile.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Staff Entity Tests")
class StaffTest {

    private Staff staff;
    private Department department;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .departmentId("dept-001")
                .departmentName("Engineering")
                .build();

        staff = Staff.builder()
                .id("staff-001")
                .name("John Doe")
                .dateOfBirth("1990-01-15")
                .phoneNumber("0123456789")
                .email("john.doe@example.com")
                .position("Software Engineer")
                .department(department)
                .build();
    }

    @Test
    @DisplayName("Should create Staff with valid data")
    void testCreateStaffWithValidData() {
        assertNotNull(staff);
        assertEquals("staff-001", staff.getId());
        assertEquals("John Doe", staff.getName());
        assertEquals("1990-01-15", staff.getDateOfBirth());
        assertEquals("0123456789", staff.getPhoneNumber());
        assertEquals("john.doe@example.com", staff.getEmail());
        assertEquals("Software Engineer", staff.getPosition());
        assertNotNull(staff.getDepartment());
    }

    @Test
    @DisplayName("Should set and get Staff name")
    void testSetAndGetName() {
        staff.setName("Jane Smith");
        assertEquals("Jane Smith", staff.getName());
    }

    @Test
    @DisplayName("Should set and get Staff date of birth")
    void testSetAndGetDateOfBirth() {
        staff.setDateOfBirth("1992-05-20");
        assertEquals("1992-05-20", staff.getDateOfBirth());
    }

    @Test
    @DisplayName("Should set and get Staff phone number")
    void testSetAndGetPhoneNumber() {
        staff.setPhoneNumber("9876543210");
        assertEquals("9876543210", staff.getPhoneNumber());
    }

    @Test
    @DisplayName("Should set and get Staff email")
    void testSetAndGetEmail() {
        staff.setEmail("jane.smith@example.com");
        assertEquals("jane.smith@example.com", staff.getEmail());
    }

    @Test
    @DisplayName("Should set and get Staff position")
    void testSetAndGetPosition() {
        staff.setPosition("Senior Developer");
        assertEquals("Senior Developer", staff.getPosition());
    }

    @Test
    @DisplayName("Should set and get Staff department")
    void testSetAndGetDepartment() {
        Department newDepartment = Department.builder()
                .departmentId("dept-002")
                .departmentName("HR")
                .build();
        staff.setDepartment(newDepartment);
        assertEquals("dept-002", staff.getDepartment().getDepartmentId());
        assertEquals("HR", staff.getDepartment().getDepartmentName());
    }

    @Test
    @DisplayName("Should set and get Staff id")
    void testSetAndGetId() {
        staff.setId("staff-002");
        assertEquals("staff-002", staff.getId());
    }

    @Test
    @DisplayName("Should create Staff with no arguments constructor")
    void testNoArgsConstructor() {
        Staff newStaff = new Staff();
        assertNull(newStaff.getId());
        assertNull(newStaff.getName());
    }

    @Test
    @DisplayName("Should create Staff with all arguments constructor")
    void testAllArgsConstructor() {
        Staff newStaff = new Staff("staff-003", "Test User", "1995-03-10", "5555555555", "test@example.com", department, "Manager");
        assertEquals("staff-003", newStaff.getId());
        assertEquals("Test User", newStaff.getName());
        assertEquals("1995-03-10", newStaff.getDateOfBirth());
        assertEquals("5555555555", newStaff.getPhoneNumber());
        assertEquals("test@example.com", newStaff.getEmail());
        assertEquals("Manager", newStaff.getPosition());
    }

    @Test
    @DisplayName("Should handle null values for optional fields")
    void testNullValues() {
        Staff newStaff = Staff.builder()
                .id("staff-004")
                .name("Null Test")
                .department(null)
                .build();
        assertNull(newStaff.getDateOfBirth());
        assertNull(newStaff.getPhoneNumber());
        assertNull(newStaff.getEmail());
        assertNull(newStaff.getPosition());
        assertNull(newStaff.getDepartment());
    }

    @Test
    @DisplayName("Should handle Staff with empty string values")
    void testEmptyStringValues() {
        Staff newStaff = Staff.builder()
                .id("staff-005")
                .name("")
                .dateOfBirth("")
                .phoneNumber("")
                .email("")
                .position("")
                .build();
        assertEquals("", newStaff.getName());
        assertEquals("", newStaff.getDateOfBirth());
        assertEquals("", newStaff.getPhoneNumber());
        assertEquals("", newStaff.getEmail());
        assertEquals("", newStaff.getPosition());
    }

    @Test
    @DisplayName("Should compare two Staff instances")
    void testStaffEquality() {
        Staff staff1 = Staff.builder()
                .id("staff-001")
                .name("John Doe")
                .build();
        Staff staff2 = Staff.builder()
                .id("staff-001")
                .name("John Doe")
                .build();
        assertEquals(staff1, staff2);
    }
}

