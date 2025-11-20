package org.pqkkkkk.hr_management_server.modules.profile.domain.service.impl;

import java.util.List;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.UserRole;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileService;
import org.pqkkkkk.hr_management_server.modules.profile.infrastructure.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getUsers(int currentPage, int pageSize, String sortBy, String sortDirection,
            String staffStatus, String department) {

        int pageIndex = Math.max(0, currentPage - 1);
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy != null && !sortBy.isBlank() ? sortBy : "createdAt");
        Pageable pageable = PageRequest.of(pageIndex, pageSize, sort);

        Specification<User> spec = Specification.where(null);

        if (staffStatus != null && !staffStatus.isBlank()) {
            try {
                UserRole role = UserRole.valueOf(staffStatus);
                spec = spec.and((root, cq, cb) -> cb.equal(root.get("role"), role));
            } catch (IllegalArgumentException ex) {
                // invalid role -> result will be empty; add false predicate
                spec = spec.and((root, cq, cb) -> cb.disjunction());
            }
        }

        if (department != null && !department.isBlank()) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("department").get("departmentId"), department));
        }

        return userRepository.findAll(spec, pageable);
    }

}
