package com.spring.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile({"local"})
public class LocalFileService implements FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private Path uploadPath;

    @Value("${file.max-per-file-bytes:10485760}") // 기본 10MB
    private long maxPerFileBytes;

    // 쉼표로 구분된 확장자 목록을 프로퍼티에서 주입
    @Value("${file.allowed-exts:pdf,doc,docx,xls,xlsx,jpg,jpeg,png}")
    private String allowedExtsProp;

    private Set<String> allowedExts;

    @PostConstruct
    public void init() throws IOException {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (Files.notExists(this.uploadPath)) {
            Files.createDirectories(this.uploadPath);
        }
        this.allowedExts = Arrays.stream(allowedExtsProp.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    @Override
    public String upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null; // 선택 파일 없음(필수면 예외로 바꿔도 됨)
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        if (!StringUtils.hasText(original)) {
            throw new IllegalArgumentException("Empty filename");
        }

        // 검증(확장자/크기)
        validateExt(original);
        validateSize(file);

        // 저장키: uuid.ext  (DB에는 이 값을 저장)
        String ext = getExtLower(original);
        String storeName = UUID.randomUUID() + (ext != null ? "." + ext : "");

        Path target = this.uploadPath.resolve(storeName).normalize();
        if (!target.startsWith(this.uploadPath)) {
            throw new SecurityException("Invalid file path");
        }

        file.transferTo(target);
        return storeName;  // ⬅️ "/uploads/..." 대신 '키'만 반환
    }

    private void validateSize(MultipartFile file) {
        if (file.getSize() > maxPerFileBytes) {
            throw new IllegalArgumentException("File too large: " + file.getOriginalFilename());
        }
    }

    private void validateExt(String filename) {
        String ext = getExtLower(filename);
        if (ext == null || !allowedExts.contains(ext)) {
            throw new IllegalArgumentException("Unsupported file type: " + filename);
        }
    }

    private static String getExtLower(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) return null;
        return filename.substring(idx + 1).toLowerCase();
    }

    private static final String URL_PREFIX = "/uploads/"; // upload()에서 붙인 프리픽스와 일치

    private String toStoreName(String storageKeyOrUrl) {
        if (storageKeyOrUrl == null) return null;
        String key = storageKeyOrUrl;
        // "/uploads/xxxxxxxx.ext" 형태면 파일명만 추출
        int slash = key.lastIndexOf('/');
        return (slash >= 0) ? key.substring(slash + 1) : key;
    }

    @Override
    public InputStream download(String storageKey) throws IOException {
        String storeName = toStoreName(storageKey);
        Path target = this.uploadPath.resolve(storeName).normalize();
        if (!target.startsWith(this.uploadPath) || Files.notExists(target)) {
            throw new IOException("File not found: " + storageKey);
        }
        return Files.newInputStream(target, StandardOpenOption.READ);
    }

    @Override
    public long contentLength(String storageKey) throws IOException {
        String storeName = toStoreName(storageKey);
        Path target = this.uploadPath.resolve(storeName).normalize();
        if (!target.startsWith(this.uploadPath) || Files.notExists(target)) {
            throw new IOException("File not found: " + storageKey);
        }
        return Files.size(target);
    }

    @Override
    public InputStream buildZipStream(List<FileEntry> files) throws IOException {
        // 메모리에 올리지 않고 스트리밍 방식으로 ZIP 만들기
        var pipeIn  = new java.io.PipedInputStream();
        var pipeOut = new java.io.PipedOutputStream(pipeIn);
        new Thread(() -> {
            try (var zos = new java.util.zip.ZipOutputStream(pipeOut)) {
                byte[] buf = new byte[8192];
                for (FileEntry fe : files) {
                    String storeName = toStoreName(fe.storageKey);
                    Path p = uploadPath.resolve(storeName).normalize();
                    if (!p.startsWith(uploadPath) || Files.notExists(p)) continue;

                    zos.putNextEntry(new java.util.zip.ZipEntry(fe.originalName));
                    try (var in = Files.newInputStream(p)) {
                        int n; while ((n = in.read(buf)) != -1) zos.write(buf, 0, n);
                    }
                    zos.closeEntry();
                }
            } catch (Exception e) {
                // 필요 시 로깅
            } finally {
                try { pipeOut.close(); } catch (Exception ignore) {}
            }
        }, "zip-writer").start();
        return pipeIn;
    }

    @Override
    public org.springframework.http.MediaType guessMediaType(String mimeFromDb, String originalName) {
        try {
            if (mimeFromDb != null && !mimeFromDb.isBlank()) {
                return org.springframework.http.MediaType.parseMediaType(mimeFromDb);
            }
        } catch (Exception ignore) {}
        String ext = getExtLower(originalName);
        if (ext == null) return org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

        return switch (ext) {
            case "pdf" -> org.springframework.http.MediaType.APPLICATION_PDF;
            case "jpg", "jpeg" -> org.springframework.http.MediaType.IMAGE_JPEG;
            case "png" -> org.springframework.http.MediaType.IMAGE_PNG;
            case "doc" -> org.springframework.http.MediaType.parseMediaType("application/msword");
            case "docx" -> org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case "xls" -> org.springframework.http.MediaType.parseMediaType("application/vnd.ms-excel");
            case "xlsx" -> org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            default -> org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}