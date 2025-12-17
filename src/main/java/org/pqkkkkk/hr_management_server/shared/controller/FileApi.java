package org.pqkkkkk.hr_management_server.shared.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response;
import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response.ApiResponse;
import org.pqkkkkk.hr_management_server.shared.storage.LocalStorageService;
import org.pqkkkkk.hr_management_server.shared.storage.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
public class FileApi {
    private final StorageService storageService;

    public FileApi(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Upload a single file. Returns a JSON response with the file access URL.
     * Accepts: multipart/form-data with field name 'file'
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @RequestParam("file") MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, false, HttpStatus.BAD_REQUEST.value(), "File is required", new Response.ApiError("file.empty")));
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename.contains("..")) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, false, HttpStatus.BAD_REQUEST.value(), "Invalid filename", new Response.ApiError("file.invalid_name")));
        }

        try {
            byte[] data = file.getBytes();
            String url = storageService.storeFile(data, originalFilename, file.getContentType());
            return ResponseEntity.ok(new ApiResponse<>(url, true, HttpStatus.OK.value(), "File uploaded", null));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to read uploaded file", new Response.ApiError(e.getMessage())));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to store file", new Response.ApiError(ex.getMessage())));
        }
    }

    /**
     * Download a file stored in local storage. This endpoint only works when the active StorageService
     * is LocalStorageService (for dev/test). For cloud storage (GCS), the storage service returns a signed URL
     * from the upload response and this GET endpoint will return 404.
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        // Prevent path traversal
        if (filename.contains("..")) {
            return ResponseEntity.badRequest().build();
        }

        if (!(storageService instanceof LocalStorageService)) {
            // For non-local storages (GCS) we expect the client to use the signed URL returned at upload time
            return ResponseEntity.notFound().build();
        }

        LocalStorageService local = (LocalStorageService) storageService;
        Path filePath = local.getFilePath(filename);

        try {
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            MediaType mediaType = (contentType != null) ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM;

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
