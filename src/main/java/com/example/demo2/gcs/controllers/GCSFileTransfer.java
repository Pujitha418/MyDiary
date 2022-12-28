package com.example.demo2.gcs.controllers;

import com.example.demo2.common.filetransfer.CloudFileTransfer;
import com.example.demo2.gcs.GCSConfigProperties;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Controller
public class GCSFileTransfer implements CloudFileTransfer {
    private GCSConfigProperties gcsConfigProperties;
    private Storage storage;
    //@Value("${gcs.project-id}")
    private final String projectId; //read from application.yaml via gcsConfigProperties

    private GoogleCredentials credentials;
    private final Logger logger;

    @Autowired
    public GCSFileTransfer(GCSConfigProperties gcsConfigProperties, InputStream credentialsFs, Logger logger) throws IOException {
        this.gcsConfigProperties = gcsConfigProperties;
        {
            assert gcsConfigProperties != null;
            projectId = this.gcsConfigProperties.projectId();
        }
        this.credentials = GoogleCredentials.fromStream(credentialsFs);
        this.storage = StorageOptions.newBuilder()
                        .setCredentials(this.credentials)
                        .setProjectId(projectId)
                        .build().getService();
        this.logger = logger;

    }

    @Override
    public String upload(String bucketName, String fileName, MultipartFile file) {
        logger.info("Inside upload");
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        try {
            byte[] data = file.getBytes();
            logger.info("data-", data.length);
            storage.create(blobInfo, data);
            logger.info("Uploaded successfully");
            return "SUCCESS";
        } catch (IOException e) {
            return "FAILED with IOError -"+e.getMessage();
        }
        catch (Exception e) {
            return "FAILED with error -"+e.getMessage();
        }
    }

    @Override
    public byte[] downloadToMemory(String bucketName, String objectName) {
        logger.info("Inside downloadToMemory");
        byte[] content = storage.readAllBytes(bucketName, objectName);
        //BufferedImage img = ImageIO.read(new ByteArrayInputStream(content));
        //System.out.println("img = " + img);
        return content;
    }
}
