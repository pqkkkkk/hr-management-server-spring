package org.pqkkkkk.hr_management_server.modules.profile.domain.service;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.filter.FilterCriteria.ProfileFilter;
import org.springframework.data.domain.Page;

public interface ProfileQueryService {
    public Page<User> getProfiles(ProfileFilter filter);
}
