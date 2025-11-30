package org.pqkkkkk.hr_management_server.shared.storage;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.SignUrlOption;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class GcsStorageService implements StorageService {

    private final Storage storage;

    @Value("${gcs.bucket-name}")
    private String bucketName;

    @Override
    public String storeFile(byte[] fileData, String originalFilename, String contentType) {
        try {
            // Generate unique filename to avoid conflicts
            String fileName = generateUniqueFileName(originalFilename);
            
            // Create BlobId
            BlobId blobId = BlobId.of(bucketName, fileName);
            
            // Create BlobInfo with metadata
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .build();
            
            // Upload file to GCS
            storage.create(blobInfo, fileData);
            
            log.info("File uploaded successfully to GCS: {}", fileName);
            
            // Generate signed URL (valid for 7 days)
            String signedUrl = generateSignedUrl(blobId);
            
            return signedUrl;
        } catch (Exception e) {
            log.error("Failed to upload file to GCS: {}", originalFilename, e);
            throw new RuntimeException("Failed to store file in Google Cloud Storage", e);
        }
    }

    /**
     * Generate a unique filename by appending UUID
     */
    private String generateUniqueFileName(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        int dotIndex = originalFilename.lastIndexOf('.');
        
        if (dotIndex > 0) {
            String nameWithoutExtension = originalFilename.substring(0, dotIndex);
            String extension = originalFilename.substring(dotIndex);
            return String.format("%s_%s%s", nameWithoutExtension, uuid, extension);
        }
        
        return String.format("%s_%s", originalFilename, uuid);
    }

    /**
     * Generate a signed URL for accessing the file
     * Signed URL is valid for 7 days
     */
    private String generateSignedUrl(BlobId blobId) {
        try {
            return storage.signUrl(
                    BlobInfo.newBuilder(blobId).build(),
                    7, // Duration
                    TimeUnit.DAYS,
                    SignUrlOption.withV4Signature()
            ).toString();
        } catch (Exception e) {
            log.warn("Failed to generate signed URL, returning public URL instead", e);
            return String.format("https://storage.googleapis.com/%s/%s", 
                    blobId.getBucket(), blobId.getName());
        }
    }

    /**
     * Delete a file from GCS (optional utility method)
     */
    public boolean deleteFile(String fileName) {
        try {
            BlobId blobId = BlobId.of(bucketName, fileName);
            boolean deleted = storage.delete(blobId);
            
            if (deleted) {
                log.info("File deleted successfully from GCS: {}", fileName);
            } else {
                log.warn("File not found in GCS: {}", fileName);
            }
            
            return deleted;
        } catch (Exception e) {
            log.error("Failed to delete file from GCS: {}", fileName, e);
            return false;
        }
    }
}
