package org.pqkkkkk.hr_management_server.shared.storage;

public interface StorageService {
    public String storeFile(byte[] fileData, String originalFilename, String contentType);
}
