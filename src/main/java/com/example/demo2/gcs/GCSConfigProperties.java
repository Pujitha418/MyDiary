package com.example.demo2.gcs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("gcs")
public record GCSConfigProperties(String credentialsFileName, String projectId, String userProfileImagesBucketName, String diaryImagesBucketName) {
}
