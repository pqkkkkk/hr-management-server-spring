package org.pqkkkkk.hr_management_server.modules.profile.domain.service;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.filter.FilterCriteria.ProfileFilter;
import org.pqkkkkk.hr_management_server.shared.Constants.SupportedFileFormat;
import org.springframework.data.domain.Page;

public interface ProfileQueryService {
    Page<User> getProfiles(ProfileFilter filter);
    User getProfileById(String userId);
    String exportProfiles(ProfileFilter filter, SupportedFileFormat fileFormat);
}
