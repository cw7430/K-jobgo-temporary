package com.spring.controller;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;

import com.spring.client.entity.CmpAttach;
import com.spring.client.repository.CmpAttachRepository;
import com.spring.service.FileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/attach")
@RequiredArgsConstructor
public class AttachController { // 기업회원가입 첨부파일 전용 

    private final CmpAttachRepository attachRepo;
    private final FileService fileService;

    @GetMapping("/{attachId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long attachId) throws IOException {
        CmpAttach a = attachRepo.findById(attachId).orElseThrow();
        InputStream in = fileService.download(a.getFPath()); // fPath = storageKey
        MediaType mt = fileService.guessMediaType(a.getFMime(), a.getOrigName());

        return ResponseEntity.ok()
            .contentType(mt)
            .contentLength(fileService.contentLength(a.getFPath()))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + URLEncoder.encode(a.getOrigName(), StandardCharsets.UTF_8) + "\"")
            .body(new InputStreamResource(in));
    }
}
