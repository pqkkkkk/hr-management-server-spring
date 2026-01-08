package org.pqkkkkk.hr_management_server.modules.profile.domain.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserGender;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserPosition;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserRole;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserSortingField;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserStatus;
import org.pqkkkkk.hr_management_server.modules.profile.domain.filter.FilterCriteria.ProfileFilter;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileQueryService;
import org.pqkkkkk.hr_management_server.shared.Constants.SortDirection;
import org.pqkkkkk.hr_management_server.shared.Constants.SupportedFileFormat;
import org.pqkkkkk.hr_management_server.shared.storage.LocalStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ProfileQueryService - Export Profiles Integration Tests")
class ProfileExportIntegrationTest {

    @Autowired
    private ProfileQueryService profileQueryService;

    @Autowired
    private LocalStorageService localStorageService;

    @AfterEach
    void cleanup() {
        // Clean up storage after each test
        localStorageService.clearStorage();
    }

    // ========================================
    // Export to Excel Tests
    // ========================================

    @Test
    @DisplayName("exportProfiles - export to Excel - should create file and return URL")
    void testExportProfiles_ExportToExcel_Success() throws IOException {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, 100,
                UserSortingField.NAME, SortDirection.ASC,
                null, null, null, null, null, null, null);

        // Act
        String fileUrl = profileQueryService.exportProfiles(filter, SupportedFileFormat.EXCEL);

        // Assert
        assertNotNull(fileUrl, "File URL should not be null");
        assertTrue(fileUrl.contains("exported_profiles"), "URL should contain 'exported_profiles'");
        assertTrue(fileUrl.contains(".xlsx"), "URL should contain Excel extension");

        // Verify file actually exists in local storage
        String filename = extractFilenameFromUrl(fileUrl);
        Path filePath = localStorageService.getFilePath(filename);
        assertTrue(Files.exists(filePath), "File should exist in local storage");

        // Verify file is not empty
        byte[] fileContent = Files.readAllBytes(filePath);
        assertTrue(fileContent.length > 0, "File should not be empty");

        // Excel files should have reasonable size (at least a few KB for headers and
        // data)
        assertTrue(fileContent.length > 1000, "Excel file should have substantial content");
    }

    @Test
    @DisplayName("exportProfiles - export to PDF - should create file and return URL")
    void testExportProfiles_ExportToPdf_Success() throws IOException {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, 100,
                UserSortingField.NAME, SortDirection.ASC,
                null, null, null, null, null, null, null);

        // Act
        String fileUrl = profileQueryService.exportProfiles(filter, SupportedFileFormat.PDF);

        // Assert
        assertNotNull(fileUrl, "File URL should not be null");
        assertTrue(fileUrl.contains("exported_profiles"), "URL should contain 'exported_profiles'");
        assertTrue(fileUrl.contains(".pdf"), "URL should contain PDF extension");

        // Verify file actually exists in local storage
        String filename = extractFilenameFromUrl(fileUrl);
        Path filePath = localStorageService.getFilePath(filename);
        assertTrue(Files.exists(filePath), "File should exist in local storage");

        // Verify file is not empty and starts with PDF header
        byte[] fileContent = Files.readAllBytes(filePath);
        assertTrue(fileContent.length > 0, "File should not be empty");

        // PDF files start with %PDF
        String pdfHeader = new String(fileContent, 0, Math.min(4, fileContent.length));
        assertEquals("%PDF", pdfHeader, "PDF file should start with %PDF header");
    }

    // ========================================
    // Export with Different Filters Tests
    // ========================================

    @Test
    @DisplayName("exportProfiles - with role filter - should export successfully")
    void testExportProfiles_WithRoleFilter_Success() throws IOException {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, 100,
                UserSortingField.NAME, SortDirection.ASC,
                null, List.of(UserRole.ADMIN), null, null, null, null, null);

        // Act
        String fileUrl = profileQueryService.exportProfiles(filter, SupportedFileFormat.EXCEL);

        // Assert
        assertNotNull(fileUrl);

        String filename = extractFilenameFromUrl(fileUrl);
        Path filePath = localStorageService.getFilePath(filename);
        assertTrue(Files.exists(filePath));

        byte[] fileContent = Files.readAllBytes(filePath);
        assertTrue(fileContent.length > 0);
    }

    @Test
    @DisplayName("exportProfiles - with status filter - should export successfully")
    void testExportProfiles_WithStatusFilter_Success() throws IOException {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, 100,
                UserSortingField.NAME, SortDirection.DESC,
                null, null, null, UserStatus.ACTIVE, null, null, null);

        // Act
        String fileUrl = profileQueryService.exportProfiles(filter, SupportedFileFormat.PDF);

        // Assert
        assertNotNull(fileUrl);

        String filename = extractFilenameFromUrl(fileUrl);
        Path filePath = localStorageService.getFilePath(filename);
        assertTrue(Files.exists(filePath));
    }

    @Test
    @DisplayName("exportProfiles - with gender filter - should export successfully")
    void testExportProfiles_WithGenderFilter_Success() throws IOException {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, 50,
                UserSortingField.NAME, SortDirection.DESC,
                null, null, UserGender.MALE, null, null, null, null);

        // Act
        String fileUrl = profileQueryService.exportProfiles(filter, SupportedFileFormat.EXCEL);

        // Assert
        assertNotNull(fileUrl);

        String filename = extractFilenameFromUrl(fileUrl);
        Path filePath = localStorageService.getFilePath(filename);
        assertTrue(Files.exists(filePath));
    }

    @Test
    @DisplayName("exportProfiles - with position filter - should export successfully")
    void testExportProfiles_WithPositionFilter_Success() throws IOException {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, 100,
                UserSortingField.NAME, SortDirection.ASC,
                null, null, null, null, UserPosition.SENIOR_DEVELOPER, null, null);

        // Act
        String fileUrl = profileQueryService.exportProfiles(filter, SupportedFileFormat.PDF);

        // Assert
        assertNotNull(fileUrl);

        String filename = extractFilenameFromUrl(fileUrl);
        Path filePath = localStorageService.getFilePath(filename);
        assertTrue(Files.exists(filePath));
    }

    @Test
    @DisplayName("exportProfiles - with multiple filters - should export successfully")
    void testExportProfiles_WithMultipleFilters_Success() throws IOException {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, 100,
                UserSortingField.NAME, SortDirection.ASC,
                null, List.of(UserRole.EMPLOYEE), UserGender.FEMALE, UserStatus.ACTIVE, null, null, null);

        // Act
        String fileUrl = profileQueryService.exportProfiles(filter, SupportedFileFormat.EXCEL);

        // Assert
        assertNotNull(fileUrl);

        String filename = extractFilenameFromUrl(fileUrl);
        Path filePath = localStorageService.getFilePath(filename);
        assertTrue(Files.exists(filePath));
    }

    // ========================================
    // Error Cases Tests
    // ========================================

    @Test
    @DisplayName("exportProfiles - no profiles found - should throw exception")
    void testExportProfiles_NoProfilesFound_ThrowsException() {
        // Arrange - Filter with name term that doesn't exist
        ProfileFilter filter = new ProfileFilter(
                1, 100,
                UserSortingField.NAME, SortDirection.ASC,
                "NonExistentUserName12345", null, null, null, null, null, null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> profileQueryService.exportProfiles(filter, SupportedFileFormat.EXCEL));

        assertEquals("No profiles found for the given filter criteria.", exception.getMessage());
    }

    // ========================================
    // File Uniqueness Tests
    // ========================================

    @Test
    @DisplayName("exportProfiles - multiple exports - should create unique files")
    void testExportProfiles_MultipleExports_CreatesUniqueFiles() throws IOException, InterruptedException {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, 100,
                UserSortingField.NAME, SortDirection.ASC,
                null, null, null, null, null, null, null);

        // Act - Export twice with slight delay to ensure different timestamps
        String fileUrl1 = profileQueryService.exportProfiles(filter, SupportedFileFormat.EXCEL);
        Thread.sleep(1000); // Wait 1 second to ensure different timestamp
        String fileUrl2 = profileQueryService.exportProfiles(filter, SupportedFileFormat.EXCEL);

        // Assert - Both files should exist and be different
        assertNotNull(fileUrl1);
        assertNotNull(fileUrl2);
        assertNotEquals(fileUrl1, fileUrl2, "File URLs should be different");

        String filename1 = extractFilenameFromUrl(fileUrl1);
        String filename2 = extractFilenameFromUrl(fileUrl2);

        Path filePath1 = localStorageService.getFilePath(filename1);
        Path filePath2 = localStorageService.getFilePath(filename2);

        assertTrue(Files.exists(filePath1), "First file should exist");
        assertTrue(Files.exists(filePath2), "Second file should exist");
        assertNotEquals(filename1, filename2, "Filenames should be different");
    }

    @Test
    @DisplayName("exportProfiles - both formats - should create different files")
    void testExportProfiles_BothFormats_CreatesDifferentFiles() throws IOException {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, 100,
                UserSortingField.NAME, SortDirection.ASC,
                null, null, null, null, null, null, null);

        // Act - Export to both Excel and PDF
        String excelUrl = profileQueryService.exportProfiles(filter, SupportedFileFormat.EXCEL);
        String pdfUrl = profileQueryService.exportProfiles(filter, SupportedFileFormat.PDF);

        // Assert
        assertNotNull(excelUrl);
        assertNotNull(pdfUrl);
        assertTrue(excelUrl.contains(".xlsx"));
        assertTrue(pdfUrl.contains(".pdf"));

        // Verify both files exist
        String excelFilename = extractFilenameFromUrl(excelUrl);
        String pdfFilename = extractFilenameFromUrl(pdfUrl);

        assertTrue(Files.exists(localStorageService.getFilePath(excelFilename)));
        assertTrue(Files.exists(localStorageService.getFilePath(pdfFilename)));
    }

    // ========================================
    // Filename Format Tests
    // ========================================

    @Test
    @DisplayName("exportProfiles - Excel format - filename contains timestamp")
    void testExportProfiles_ExcelFormat_FilenameContainsTimestamp() {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, 100,
                UserSortingField.NAME, SortDirection.ASC,
                null, null, null, null, null, null, null);

        // Act
        String fileUrl = profileQueryService.exportProfiles(filter, SupportedFileFormat.EXCEL);

        // Assert
        assertNotNull(fileUrl);
        String filename = extractFilenameFromUrl(fileUrl);

        // Check filename pattern: exported_profiles_yyyyMMdd_HHmmss_uuid.xlsx
        assertTrue(filename.startsWith("exported_profiles_"), "Filename should start with 'exported_profiles_'");
        assertTrue(filename.matches("exported_profiles_\\d{8}_\\d{6}.*\\.xlsx"),
                "Filename should match pattern: exported_profiles_yyyyMMdd_HHmmss_*.xlsx");
    }

    @Test
    @DisplayName("exportProfiles - PDF format - filename contains timestamp")
    void testExportProfiles_PdfFormat_FilenameContainsTimestamp() {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, 100,
                UserSortingField.NAME, SortDirection.ASC,
                null, null, null, null, null, null, null);

        // Act
        String fileUrl = profileQueryService.exportProfiles(filter, SupportedFileFormat.PDF);

        // Assert
        assertNotNull(fileUrl);
        String filename = extractFilenameFromUrl(fileUrl);

        // Check filename pattern: exported_profiles_yyyyMMdd_HHmmss_uuid.pdf
        assertTrue(filename.startsWith("exported_profiles_"), "Filename should start with 'exported_profiles_'");
        assertTrue(filename.matches("exported_profiles_\\d{8}_\\d{6}.*\\.pdf"),
                "Filename should match pattern: exported_profiles_yyyyMMdd_HHmmss_*.pdf");
    }

    // ========================================
    // Large Dataset Tests
    // ========================================

    @Test
    @DisplayName("exportProfiles - large dataset - should handle successfully")
    void testExportProfiles_LargeDataset_Success() throws IOException {
        // Arrange - Get all profiles with large page size
        ProfileFilter filter = new ProfileFilter(
                1, 1000, // Large page size
                UserSortingField.NAME, SortDirection.ASC,
                null, null, null, null, null, null, null);

        // Act
        String fileUrl = profileQueryService.exportProfiles(filter, SupportedFileFormat.EXCEL);

        // Assert
        assertNotNull(fileUrl);

        String filename = extractFilenameFromUrl(fileUrl);
        Path filePath = localStorageService.getFilePath(filename);
        assertTrue(Files.exists(filePath));

        // Verify file size is reasonable (not empty, not too small)
        byte[] fileContent = Files.readAllBytes(filePath);
        assertTrue(fileContent.length > 1000, "File should contain substantial data for large dataset");
    }

    // ========================================
    // Storage Integration Tests
    // ========================================

    @Test
    @DisplayName("exportProfiles - file stored with correct content type - Excel")
    void testExportProfiles_FileStoredWithCorrectContentType_Excel() throws IOException {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, 100,
                UserSortingField.NAME, SortDirection.ASC,
                null, null, null, null, null, null, null);

        // Act
        String fileUrl = profileQueryService.exportProfiles(filter, SupportedFileFormat.EXCEL);

        // Assert
        assertNotNull(fileUrl);

        // Verify file is valid Excel by checking file signature
        String filename = extractFilenameFromUrl(fileUrl);
        Path filePath = localStorageService.getFilePath(filename);
        byte[] fileContent = Files.readAllBytes(filePath);

        // Excel files (xlsx) are ZIP archives, should start with PK (50 4B)
        assertTrue(fileContent.length > 4);
        assertEquals(0x50, fileContent[0] & 0xFF, "Excel file should start with PK header (0x50)");
        assertEquals(0x4B, fileContent[1] & 0xFF, "Excel file should start with PK header (0x4B)");
    }

    @Test
    @DisplayName("exportProfiles - file stored with correct content type - PDF")
    void testExportProfiles_FileStoredWithCorrectContentType_PDF() throws IOException {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, 100,
                UserSortingField.NAME, SortDirection.ASC,
                null, null, null, null, null, null, null);

        // Act
        String fileUrl = profileQueryService.exportProfiles(filter, SupportedFileFormat.PDF);

        // Assert
        assertNotNull(fileUrl);

        // Verify file is valid PDF by checking file signature
        String filename = extractFilenameFromUrl(fileUrl);
        Path filePath = localStorageService.getFilePath(filename);
        byte[] fileContent = Files.readAllBytes(filePath);

        // PDF files should start with %PDF
        assertTrue(fileContent.length > 4);
        String header = new String(fileContent, 0, 4);
        assertEquals("%PDF", header, "PDF file should start with %PDF header");
    }

    // ========================================
    // Helper Methods
    // ========================================

    private String extractFilenameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }
}
