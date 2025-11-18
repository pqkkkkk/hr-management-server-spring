package org.pqkkkkk.hr_management_server.modules.profile.domain.service;

import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response;

public interface ProfileServiceI {
    Response getAllProfiles();
    Response getProfileById(String id);
    Response updateProfile(String id, Object profileData);
}
