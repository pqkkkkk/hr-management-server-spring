package org.pqkkkkk.hr_management_server.modules.profile.controller.http;

import java.util.List;
import java.util.stream.Collectors;

import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.PagedResult;
import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.ProfileDTO;
import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response.ApiResponse;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProfileApi {

	private final ProfileService profileService;

	@GetMapping("/users")
	public ResponseEntity<ApiResponse<PagedResult<ProfileDTO>>> getAllUsers(
			@RequestParam(name = "currentPage", required = false, defaultValue = "1") int currentPage,
			@RequestParam(name = "pageSize", required = false, defaultValue = "20") int pageSize,
			@RequestParam(name = "sortBy", required = false, defaultValue = "createdAt") String sortBy,
			@RequestParam(name = "sortDirection", required = false, defaultValue = "desc") String sortDirection,
			@RequestParam(name = "staffStatus", required = false) String staffStatus,
			@RequestParam(name = "department", required = false) String department
	) {
		Page<User> page = profileService.getUsers(currentPage, pageSize, sortBy, sortDirection, staffStatus,
				department);
		List<ProfileDTO> dtos = page.getContent().stream().map(ProfileDTO::fromEntity).collect(Collectors.toList());
		PagedResult<ProfileDTO> paged = new PagedResult<>(dtos, page.getNumber() + 1, page.getSize(), page.getTotalElements(),
				page.getTotalPages());
		ApiResponse<PagedResult<ProfileDTO>> response = new ApiResponse<>(paged, true, HttpStatus.OK.value(), "OK",
				null);
		return ResponseEntity.ok(response);
	}

}
