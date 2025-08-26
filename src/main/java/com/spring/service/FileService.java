/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.spring.service.FileService
 *  org.springframework.web.multipart.MultipartFile
 */
package com.spring.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    // 업로드 (기존)
    String upload(MultipartFile file) throws IOException;

    // ✅ 단일 파일 다운로드용: 저장키로 InputStream 열기
    InputStream download(String storageKey) throws IOException;

    // ✅ (선택) 저장소에 있는 실제 콘텐츠 길이 조회
    long contentLength(String storageKey) throws IOException;

    // ✅ 파일 여러 개를 ZIP으로 묶어 InputStream 생성
    //   - storageKey/originalName 필요하므로 DTO 형태로 받거나 필요한 정보만 받도록 설계
    InputStream buildZipStream(List<FileEntry> files) throws IOException;

    // ✅ 간단한 MIME 추정 (원본 MIME/파일명 기반)
    MediaType guessMediaType(String mimeFromDb, String originalName);

    // ZIP에 넣을 파일 엔트리(간단 DTO)
    class FileEntry {
        public final String storageKey;
        public final String originalName;

        public FileEntry(String storageKey, String originalName) {
            this.storageKey = storageKey;
            this.originalName = originalName;
        }
    }
}

