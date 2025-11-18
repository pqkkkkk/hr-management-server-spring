package org.pqkkkkk.hr_management_server.modules.profile.domain.service;

import lombok.Generated;
import lombok.RequiredArgsConstructor;
import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.StaffDTO;
import org.pqkkkkk.hr_management_server.modules.profile.domain.Mappers.StaffMapper;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Staff;
import org.pqkkkkk.hr_management_server.modules.profile.domain.repositories.StaffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffServiceIplm implements StaffService {

    private final StaffRepository staffRepository;
    private final StaffMapper staffMapper;

    @Override
    public StaffDTO createStaff(StaffDTO staffDTO) {
        Staff staff = Staff.builder()
                .name(staffDTO.getName())
                .dateOfBirth(staffDTO.getDateOfBirth())
                .email(staffDTO.getEmail())
                .phoneNumber(staffDTO.getPhoneNumber())
                .position(staffDTO.getPosition())
                .build();
        Staff savedStaff = staffRepository.save(staff);
        return staffMapper.toDTO(savedStaff);
    }

    @Override
    public StaffDTO updateStaff(String id, StaffDTO staffDTO) {
        Staff existingStaff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));

        existingStaff.setName(staffDTO.getName());
        existingStaff.setDateOfBirth(staffDTO.getDateOfBirth());
        existingStaff.setEmail(staffDTO.getEmail());
        existingStaff.setPhoneNumber(staffDTO.getPhoneNumber());
        existingStaff.setPosition(staffDTO.getPosition());

        Staff updatedStaff = staffRepository.save(existingStaff);
        return staffMapper.toDTO(updatedStaff);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffDTO getStaffById(String id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));
        return staffMapper.toDTO(staff);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffDTO> getAllStaff() {
        return staffRepository.findAll().stream()
                .map(staffMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteStaff(String id) {
        if (!staffRepository.existsById(id)) {
            throw new RuntimeException("Staff not found with id: " + id);
        }
        staffRepository.deleteById(id);
    }
}

