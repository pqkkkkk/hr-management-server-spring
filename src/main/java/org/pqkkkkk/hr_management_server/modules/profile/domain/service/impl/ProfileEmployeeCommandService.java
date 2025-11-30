package org.pqkkkkk.hr_management_server.modules.profile.domain.service.impl;

import org.pqkkkkk.hr_management_server.modules.profile.domain.dao.ProfileDao;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileCommandService;
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

		// Kiểm tra user tồn tại
		User existing = profileDao.getProfileById(user.getUserId());
		if (existing == null) {
			throw new IllegalArgumentException("User does not exist");
		}

		// Chỉ cập nhật các trường được gửi lên (partial update)
		if (user.getFullName() != null && StringUtils.hasText(user.getFullName())) {
			existing.setFullName(user.getFullName());
		}
		if (user.getEmail() != null && StringUtils.hasText(user.getEmail())) {
			existing.setEmail(user.getEmail());
		}
		if (user.getPhoneNumber() != null && StringUtils.hasText(user.getPhoneNumber())) {
			existing.setPhoneNumber(user.getPhoneNumber());
		}
		if (user.getAddress() != null && StringUtils.hasText(user.getAddress())) {
			existing.setAddress(user.getAddress());
		}

		// Gọi DAO để lưu
		return profileDao.updateProfile(existing);
	}

	@Override
	public User deactivateUser(String userId) {
		throw new UnsupportedOperationException("Employee cannot deactivate user");
	}
}
