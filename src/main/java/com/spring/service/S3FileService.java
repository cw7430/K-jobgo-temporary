/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.spring.service.FileService
 *  com.spring.service.S3FileService
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.context.annotation.Profile
 *  org.springframework.stereotype.Service
 *  org.springframework.web.multipart.MultipartFile
 *  software.amazon.awssdk.core.sync.RequestBody
 *  software.amazon.awssdk.services.s3.S3Client
 *  software.amazon.awssdk.services.s3.model.ObjectCannedACL
 *  software.amazon.awssdk.services.s3.model.PutObjectRequest
 */
package com.spring.service;

import com.spring.service.FileService;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Profile(value={"prod"})
public class S3FileService
implements FileService {
    private final S3Client s3;
    private final String bucket;
    private final String region;

    public S3FileService(S3Client s3, @Value(value="${aws.s3.bucket}") String bucket, @Value(value="${aws.region}") String region) {
        this.s3 = s3;
        this.bucket = bucket;
        this.region = region;
    }

    private void validateFileExtension(String filename) {
        String extension;
        List<String> allowedExtensions = List.of(".jpg", ".jpeg", ".png", ".gif");
        if (!allowedExtensions.contains(extension = filename.substring(filename.lastIndexOf(".")).toLowerCase())) {
            throw new IllegalArgumentException("Unsupported file type: " + extension);
        }
    }

    public String upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String original = file.getOriginalFilename();
        this.validateFileExtension(original);
        String key = "profiles/" + String.valueOf(UUID.randomUUID()) + "-" + original;
        PutObjectRequest por = (PutObjectRequest)PutObjectRequest.builder().bucket(this.bucket).key(key).contentType(file.getContentType()).acl(ObjectCannedACL.PUBLIC_READ).build();
        this.s3.putObject(por, RequestBody.fromInputStream((InputStream)file.getInputStream(), (long)file.getSize()));
        return String.format("https://%s.s3.%s.amazonaws.com/%s", this.bucket, this.region, key);
    }
}

