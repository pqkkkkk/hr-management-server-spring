package org.pqkkkkk.hr_management_server.modules.profile.domain.service;

import java.util.List;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.springframework.data.domain.Page;

public interface ProfileService {
	List<User> getAllUsers();

	Page<User> getUsers(int currentPage, int pageSize, String sortBy, String sortDirection,
			String staffStatus, String department);

}
