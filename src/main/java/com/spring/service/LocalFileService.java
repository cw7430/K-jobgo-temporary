/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.spring.service.FileService
 *  com.spring.service.LocalFileService
 *  jakarta.annotation.PostConstruct
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.context.annotation.Profile
 *  org.springframework.stereotype.Service
 *  org.springframework.util.StringUtils
 *  org.springframework.web.multipart.MultipartFile
 */
package com.spring.service;

import com.spring.service.FileService;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile(value={"local"})
public class LocalFileService
implements FileService {
    @Value(value="${file.upload-dir}")
    private String uploadDir;
    private Path uploadPath;

    @PostConstruct
    public void init() throws IOException {
        this.uploadPath = Paths.get(this.uploadDir, new String[0]).toAbsolutePath().normalize();
        System.out.println(">>> LocalFileService uploadPath = " + String.valueOf(this.uploadPath));
        if (Files.notExists(this.uploadPath, new LinkOption[0])) {
            Files.createDirectories(this.uploadPath, new FileAttribute[0]);
        }
    }

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String original = StringUtils.cleanPath((String)file.getOriginalFilename());
        String ext = StringUtils.getFilenameExtension((String)original);
        String storeName = UUID.randomUUID().toString() + (String)(ext != null ? "." + ext : "");
        try {
            Path target = this.uploadPath.resolve(storeName).normalize();
            if (!target.startsWith(this.uploadPath)) {
                throw new SecurityException("\uc798\ubabb\ub41c \ud30c\uc77c \uacbd\ub85c\uc785\ub2c8\ub2e4.");
            }
            file.transferTo(target);
            System.out.println(">>> \uc800\uc7a5 \ud30c\uc77c \uacbd\ub85c = " + String.valueOf(target.toAbsolutePath()));
            System.out.println(">>> \uc6f9\uc5d0\uc11c \uc811\uadfc\ud560 photoUrl = /uploads/" + storeName);
            System.out.println(">>> \uc5c5\ub85c\ub4dc \uc2dc\ub3c4 \ud30c\uc77c\uba85: " + original);
            System.out.println(">>> \uc800\uc7a5 \uacbd\ub85c: " + String.valueOf(this.uploadPath.resolve(storeName).normalize()));
            return "/uploads/" + storeName;
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("\ud30c\uc77c \uc5c5\ub85c\ub4dc \uc911 \uc624\ub958\uac00 \ubc1c\uc0dd\ud588\uc2b5\ub2c8\ub2e4.", e);
        }
    }
}

