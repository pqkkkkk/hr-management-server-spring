package org.pqkkkkk.hr_management_server.modules.profile.domain.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.pqkkkkk.hr_management_server.modules.profile.domain.dao.ProfileDao;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.ExportedUser;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.filter.FilterCriteria.ProfileFilter;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileQueryService;
import org.pqkkkkk.hr_management_server.shared.Constants.SupportedFileFormat;
import org.pqkkkkk.hr_management_server.shared.file.FileService;
import org.pqkkkkk.hr_management_server.shared.storage.StorageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ProfileQueryServiceImpl implements ProfileQueryService {
    private final ProfileDao profileDao;
    private final StorageService storageService;
    private final Map<SupportedFileFormat, FileService<ExportedUser>> fileServices;

    public ProfileQueryServiceImpl(ProfileDao profileDao, StorageService storageService,
            @Qualifier("profileFileServices") Map<SupportedFileFormat, FileService<ExportedUser>> fileServices) {
        this.profileDao = profileDao;
        this.storageService = storageService;
        this.fileServices = fileServices;
    }

    @Override
    public Page<User> getProfiles(ProfileFilter filter) {
        return profileDao.getProfiles(filter);
    }

    @Override
    public User getProfileById(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be null or blank");
        }
        User user = profileDao.getProfileById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User with id '" + userId + "' does not exist");
        }
        return user;
    }

    @Override
    public String exportProfiles(ProfileFilter filter, SupportedFileFormat fileFormat) {
        List<User> users = profileDao.getAllProfiles(filter);

        if (users.isEmpty()) {
            throw new IllegalArgumentException("No profiles found for the given filter criteria.");
        }

        List<ExportedUser> exportedUsers = users.stream()
                .map(ExportedUser::new)
                .toList();

        byte[] fileData = getFileService(fileFormat).exportListToFile(exportedUsers);

        String fileName = generateFileName("exported_profiles", fileFormat);

        String fileUrl = storageService.storeFile(fileData, fileName, fileFormat.getContentType());

        return fileUrl;
    }

    private FileService<ExportedUser> getFileService(SupportedFileFormat fileFormat) {
        FileService<ExportedUser> fileService = fileServices.get(fileFormat);
        if (fileService == null) {
            throw new IllegalArgumentException("Unsupported file format: " + fileFormat);
        }
        return fileService;
    }

    private String generateFileName(String prefix, SupportedFileFormat format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%s%s", prefix, timestamp, format.getFileExtension());
    }

}
