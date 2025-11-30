package org.pqkkkkk.hr_management_server.shared.config;

import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Configuration
@Profile("!test")
public class GcsConfig {

    @Value("${gcs.project-id}")
    private String projectId;

    @Value("${gcs.credentials-path}")
    private Resource credentialsResource;

    @Bean
    public Storage googleCloudStorage() throws IOException {
        GoogleCredentials credentials;

        // Load credentials from file or use default credentials
        if (credentialsResource != null && credentialsResource.exists()) {
            try (FileInputStream serviceAccountStream = new FileInputStream(credentialsResource.getFile())) {
                credentials = GoogleCredentials.fromStream(serviceAccountStream);
            }
        } else {
            // Use Application Default Credentials (for GCP environments)
            credentials = GoogleCredentials.getApplicationDefault();
        }

        return StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();
    }
}
