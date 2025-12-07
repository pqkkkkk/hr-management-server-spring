package org.pqkkkkk.hr_management_server.shared.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Profile({"docker", "test"})
@Slf4j
public class LocalStorageService implements StorageService {

    @Value("${storage.local.directory:${java.io.tmpdir}/hr-management-test-storage}")
    private String storageDirectory;

    @Value("${storage.local.base-url:http://localhost:8080/api/files}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        try {
            Path storagePath = Paths.get(storageDirectory);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
                log.info("Created local storage directory: {}", storageDirectory);
            } else {
                log.info("Using existing local storage directory: {}", storageDirectory);
            }
        } catch (IOException e) {
            log.error("Failed to create local storage directory: {}", storageDirectory, e);
            throw new RuntimeException("Failed to initialize local storage", e);
        }
    }

    @Override
    public String storeFile(byte[] fileData, String originalFilename, String contentType) {
        try {
            // Generate unique filename to avoid conflicts
            String uniqueFilename = generateUniqueFileName(originalFilename);
            
            // Create file path
            Path filePath = Paths.get(storageDirectory, uniqueFilename);
            
            // Write file to local storage
            Files.write(filePath, fileData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            
            log.info("File stored successfully in local storage: {}", uniqueFilename);
            
            // Return URL to access the file
            String fileUrl = baseUrl + "/" + uniqueFilename;
            
            return fileUrl;
        } catch (IOException e) {
            log.error("Failed to store file locally: {}", originalFilename, e);
            throw new RuntimeException("Failed to store file in local storage", e);
        }
    }

    /**
     * Generate a unique filename by appending UUID
     */
    private String generateUniqueFileName(String originalFilename) {
        String uuid = UUID.randomUUID().toString().substring(0, 8); // Short UUID for readability
        int dotIndex = originalFilename.lastIndexOf('.');
        
        if (dotIndex > 0) {
            String nameWithoutExtension = originalFilename.substring(0, dotIndex);
            String extension = originalFilename.substring(dotIndex);
            return String.format("%s_%s%s", nameWithoutExtension, uuid, extension);
        }
        
        return String.format("%s_%s", originalFilename, uuid);
    }

    /**
     * Get the actual file path (useful for testing)
     */
    public Path getFilePath(String filename) {
        return Paths.get(storageDirectory, filename);
    }

    /**
     * Delete a file from local storage (useful for test cleanup)
     */
    public boolean deleteFile(String filename) {
        try {
            Path filePath = Paths.get(storageDirectory, filename);
            boolean deleted = Files.deleteIfExists(filePath);
            
            if (deleted) {
                log.info("File deleted successfully from local storage: {}", filename);
            } else {
                log.warn("File not found in local storage: {}", filename);
            }
            
            return deleted;
        } catch (IOException e) {
            log.error("Failed to delete file from local storage: {}", filename, e);
            return false;
        }
    }

    /**
     * Clear all files in storage directory (useful for test cleanup)
     */
    public void clearStorage() {
        try {
            Path storagePath = Paths.get(storageDirectory);
            if (Files.exists(storagePath)) {
                Files.walk(storagePath)
                        .filter(Files::isRegularFile)
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                                log.debug("Deleted file: {}", path.getFileName());
                            } catch (IOException e) {
                                log.warn("Failed to delete file: {}", path.getFileName(), e);
                            }
                        });
                log.info("Cleared all files from local storage");
            }
        } catch (IOException e) {
            log.error("Failed to clear local storage", e);
        }
    }
}
