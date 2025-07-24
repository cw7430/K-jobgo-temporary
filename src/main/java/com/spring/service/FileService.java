/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.spring.service.FileService
 *  org.springframework.web.multipart.MultipartFile
 */
package com.spring.service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    public String upload(MultipartFile file) throws IOException;
}

