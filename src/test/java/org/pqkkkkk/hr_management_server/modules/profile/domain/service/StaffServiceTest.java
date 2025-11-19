package org.pqkkkkk.hr_management_server.modules.profile.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.StaffDTO;
import org.pqkkkkk.hr_management_server.modules.profile.domain.Mappers.StaffMapper;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Department;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Staff;
import org.pqkkkkk.hr_management_server.modules.profile.domain.repositories.StaffRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Staff Service Tests")
class StaffServiceTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private StaffMapper staffMapper;

    @InjectMocks
    private StaffServiceIplm staffService;

    private StaffDTO staffDTO;
    private Staff staff;
    private Department department;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .departmentId("dept-001")
                .departmentName("Engineering")
                .build();

        staffDTO = new StaffDTO();
        staffDTO.setId("staff-001");
        staffDTO.setName("John Doe");
        staffDTO.setDateOfBirth("1990-01-15");
        staffDTO.setPhoneNumber("0123456789");
        staffDTO.setEmail("john.doe@example.com");
        staffDTO.setPosition("Software Engineer");
        staffDTO.setDepartmentId("dept-001");
        staffDTO.setDepartmentName("Engineering");

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
    @DisplayName("Should create Staff successfully")
    void testCreateStaffSuccess() {
        // Arrange
        when(staffRepository.save(any(Staff.class))).thenReturn(staff);
        when(staffMapper.toDTO(staff)).thenReturn(staffDTO);

        // Act
        StaffDTO result = staffService.createStaff(staffDTO);

        // Assert
        assertNotNull(result);
        assertEquals("staff-001", result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john.doe@example.com", result.getEmail());
        verify(staffRepository, times(1)).save(any(Staff.class));
        verify(staffMapper, times(1)).toDTO(staff);
    }

    @Test
    @DisplayName("Should create Staff without department")
    void testCreateStaffWithoutDepartment() {
        // Arrange
        StaffDTO dtoWithoutDept = new StaffDTO();
        dtoWithoutDept.setName("Jane Smith");
        dtoWithoutDept.setEmail("jane@example.com");

        Staff staffWithoutDept = Staff.builder()
                .name("Jane Smith")
                .email("jane@example.com")
                .build();

        StaffDTO resultDTO = new StaffDTO();
        resultDTO.setName("Jane Smith");
        resultDTO.setEmail("jane@example.com");

        when(staffRepository.save(any(Staff.class))).thenReturn(staffWithoutDept);
        when(staffMapper.toDTO(staffWithoutDept)).thenReturn(resultDTO);

        // Act
        StaffDTO result = staffService.createStaff(dtoWithoutDept);

        // Assert
        assertNotNull(result);
        assertEquals("Jane Smith", result.getName());
        verify(staffRepository, times(1)).save(any(Staff.class));
    }

    @Test
    @DisplayName("Should update Staff successfully")
    void testUpdateStaffSuccess() {
        // Arrange
        StaffDTO updateDTO = new StaffDTO();
        updateDTO.setName("John Updated");
        updateDTO.setPosition("Senior Engineer");
        updateDTO.setEmail("john.updated@example.com");
        updateDTO.setDateOfBirth("1990-01-15");
        updateDTO.setPhoneNumber("0123456789");

        Staff updatedStaff = Staff.builder()
                .id("staff-001")
                .name("John Updated")
                .position("Senior Engineer")
                .email("john.updated@example.com")
                .dateOfBirth("1990-01-15")
                .phoneNumber("0123456789")
                .build();

        StaffDTO updatedDTO = new StaffDTO();
        updatedDTO.setId("staff-001");
        updatedDTO.setName("John Updated");
        updatedDTO.setPosition("Senior Engineer");

        when(staffRepository.findById("staff-001")).thenReturn(Optional.of(staff));
        when(staffRepository.save(any(Staff.class))).thenReturn(updatedStaff);
        when(staffMapper.toDTO(updatedStaff)).thenReturn(updatedDTO);

        // Act
        StaffDTO result = staffService.updateStaff("staff-001", updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("John Updated", result.getName());
        assertEquals("Senior Engineer", result.getPosition());
        verify(staffRepository, times(1)).findById("staff-001");
        verify(staffRepository, times(1)).save(any(Staff.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent Staff")
    void testUpdateStaffNotFound() {
        // Arrange
        when(staffRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            staffService.updateStaff("invalid-id", staffDTO);
        });
        verify(staffRepository, times(1)).findById("invalid-id");
        verify(staffRepository, never()).save(any(Staff.class));
    }

    @Test
    @DisplayName("Should get Staff by ID successfully")
    void testGetStaffByIdSuccess() {
        // Arrange
        when(staffRepository.findById("staff-001")).thenReturn(Optional.of(staff));
        when(staffMapper.toDTO(staff)).thenReturn(staffDTO);

        // Act
        StaffDTO result = staffService.getStaffById("staff-001");

        // Assert
        assertNotNull(result);
        assertEquals("staff-001", result.getId());
        assertEquals("John Doe", result.getName());
        verify(staffRepository, times(1)).findById("staff-001");
        verify(staffMapper, times(1)).toDTO(staff);
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent Staff")
    void testGetStaffByIdNotFound() {
        // Arrange
        when(staffRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            staffService.getStaffById("invalid-id");
        });
        verify(staffRepository, times(1)).findById("invalid-id");
    }

    @Test
    @DisplayName("Should get all Staff successfully")
    void testGetAllStaffSuccess() {
        // Arrange
        Staff staff2 = Staff.builder()
                .id("staff-002")
                .name("Jane Smith")
                .email("jane@example.com")
                .build();

        List<Staff> staffList = Arrays.asList(staff, staff2);

        StaffDTO staffDTO2 = new StaffDTO();
        staffDTO2.setId("staff-002");
        staffDTO2.setName("Jane Smith");
        staffDTO2.setEmail("jane@example.com");

        when(staffRepository.findAll()).thenReturn(staffList);
        when(staffMapper.toDTO(staff)).thenReturn(staffDTO);
        when(staffMapper.toDTO(staff2)).thenReturn(staffDTO2);

        // Act
        List<StaffDTO> result = staffService.getAllStaff();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Smith", result.get(1).getName());
        verify(staffRepository, times(1)).findAll();
        verify(staffMapper, times(2)).toDTO(any(Staff.class));
    }

    @Test
    @DisplayName("Should return empty list when no Staff exists")
    void testGetAllStaffEmpty() {
        // Arrange
        when(staffRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<StaffDTO> result = staffService.getAllStaff();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(staffRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should delete Staff successfully")
    void testDeleteStaffSuccess() {
        // Arrange
        when(staffRepository.existsById("staff-001")).thenReturn(true);
        doNothing().when(staffRepository).deleteById("staff-001");

        // Act
        assertDoesNotThrow(() -> staffService.deleteStaff("staff-001"));

        // Assert
        verify(staffRepository, times(1)).existsById("staff-001");
        verify(staffRepository, times(1)).deleteById("staff-001");
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent Staff")
    void testDeleteStaffNotFound() {
        // Arrange
        when(staffRepository.existsById("invalid-id")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            staffService.deleteStaff("invalid-id");
        });
        verify(staffRepository, times(1)).existsById("invalid-id");
        verify(staffRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("Should handle Staff with special characters in fields")
    void testCreateStaffWithSpecialCharacters() {
        // Arrange
        StaffDTO specialDTO = new StaffDTO();
        specialDTO.setName("John O'Brien");
        specialDTO.setEmail("john+test@example.com");
        specialDTO.setPhoneNumber("+1-800-123-4567");

        Staff specialStaff = Staff.builder()
                .name("John O'Brien")
                .email("john+test@example.com")
                .phoneNumber("+1-800-123-4567")
                .build();

        StaffDTO resultDTO = new StaffDTO();
        resultDTO.setName("John O'Brien");
        resultDTO.setEmail("john+test@example.com");
        resultDTO.setPhoneNumber("+1-800-123-4567");

        when(staffRepository.save(any(Staff.class))).thenReturn(specialStaff);
        when(staffMapper.toDTO(specialStaff)).thenReturn(resultDTO);

        // Act
        StaffDTO result = staffService.createStaff(specialDTO);

        // Assert
        assertNotNull(result);
        assertEquals("John O'Brien", result.getName());
        assertEquals("john+test@example.com", result.getEmail());
        verify(staffRepository, times(1)).save(any(Staff.class));
    }
}

