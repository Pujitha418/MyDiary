package com.example.demo2.common.filetransfer;

import org.springframework.web.multipart.MultipartFile;

public interface CloudFileTransfer {
    public String upload(String bucketName, String fileName, MultipartFile file);

    public byte[] downloadToMemory(String bucketName, String objectName);
}
