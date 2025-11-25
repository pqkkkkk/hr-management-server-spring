package org.pqkkkkk.hr_management_server.modules.profile.domain.service.impl;

import org.pqkkkkk.hr_management_server.modules.profile.domain.dao.ProfileDao;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileCommandService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProfileEmployeeCommandService implements ProfileCommandService {
	private final ProfileDao profileDao;

	public ProfileEmployeeCommandService(ProfileDao profileDao) {
		this.profileDao = profileDao;
	}

	@Override
	@Transactional
	public User updateProfile(User user) {
		// Validate input
		if (user == null) {
			throw new IllegalArgumentException("User cannot be null");
		}
		if (!StringUtils.hasText(user.getUserId())) {
			throw new IllegalArgumentException("UserId is required");
		}
		if (!StringUtils.hasText(user.getEmail())) {
			throw new IllegalArgumentException("Email is required");
		}
		if (!StringUtils.hasText(user.getPhoneNumber())) {
			throw new IllegalArgumentException("Phone number is required");
		}
		if (!StringUtils.hasText(user.getAddress())) {
			throw new IllegalArgumentException("Address is required");
		}

		// Kiểm tra user tồn tại
		User existing = profileDao.getProfileById(user.getUserId());
		if (existing == null) {
			throw new IllegalArgumentException("User does not exist");
		}

		// Chỉ cập nhật các trường cho phép
		existing.setEmail(user.getEmail());
		existing.setPhoneNumber(user.getPhoneNumber());
		existing.setAddress(user.getAddress());

	
		// Gọi DAO để lưu
		return profileDao.updateProfile(existing);
	}
}
