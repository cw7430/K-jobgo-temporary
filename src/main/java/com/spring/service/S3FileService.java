package com.spring.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile({"prod"})
public class S3FileService implements FileService {

    private final S3Client s3;
    private final String bucket;
    private final String region;

    public S3FileService(
            S3Client s3,
            @Value("${aws.s3.bucket}") String bucket,
            @Value("${aws.region}") String region
    ) {
        this.s3 = s3;
        this.bucket = bucket;
        this.region = region;
    }

    /** 업로드 키 접두사 (예: uploads/ 또는 profiles/) */
    @Value("${aws.s3.prefix:uploads/}")
    private String keyPrefixProp;

    /** 허용 확장자: 쉼표 구분 (기본: pdf,doc,docx,xls,xlsx,jpg,jpeg,png) */
    @Value("${file.allowed-exts:pdf,doc,docx,xls,xlsx,jpg,jpeg,png}")
    private String allowedExtsProp;

    /** 파일 최대 크기(byte). 기본 10MB */
    @Value("${file.max-per-file-bytes:10485760}")
    private long maxPerFileBytes;

    private Set<String> allowedExts;
    private String keyPrefix; // 끝에 항상 '/'가 붙은 상태로 보관

    @PostConstruct
    public void init() {
        this.allowedExts = Arrays.stream(allowedExtsProp.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        String p = keyPrefixProp == null ? "" : keyPrefixProp.trim();
        if (p.isEmpty()) p = "uploads/";
        if (!p.endsWith("/")) p = p + "/";
        this.keyPrefix = p;
    }

    @Override
    public String upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null; // 선택 파일 없음
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        if (!StringUtils.hasText(original)) {
            throw new IllegalArgumentException("Empty filename");
        }

        validateExt(original);
        validateSize(file);

        // 저장 키: prefix + uuid.ext
        String ext = getExtLower(original);
        String key = keyPrefix + UUID.randomUUID() + "." + ext;

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        s3.putObject(putReq, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return key;  // ✅ 풀 URL 대신 "저장키"만 반환
    }

    /* ===== 내부 검증/유틸 ===== */

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
    
    @Override
    public InputStream download(String storageKey) throws IOException {
        try {
            var getReq = software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                    .bucket(bucket).key(storageKey).build();
            return s3.getObject(getReq); // Close는 컨트롤러가 InputStreamResource로 처리
        } catch (Exception e) {
            throw new IOException("S3 getObject failed: " + storageKey, e);
        }
    }

    @Override
    public long contentLength(String storageKey) throws IOException {
        try {
            var head = s3.headObject(b -> b.bucket(bucket).key(storageKey));
            return head.contentLength();
        } catch (Exception e) {
            throw new IOException("S3 headObject failed: " + storageKey, e);
        }
    }

    @Override
    public InputStream buildZipStream(List<FileEntry> files) throws IOException {
        var pin  = new java.io.PipedInputStream();
        var pout = new java.io.PipedOutputStream(pin);
        new Thread(() -> {
            try (var zos = new java.util.zip.ZipOutputStream(pout)) {
                byte[] buf = new byte[8192];
                for (FileEntry fe : files) {
                    zos.putNextEntry(new java.util.zip.ZipEntry(fe.originalName));
                    try (var in = download(fe.storageKey)) {
                        int n; while ((n = in.read(buf)) != -1) zos.write(buf, 0, n);
                    }
                    zos.closeEntry();
                }
            } catch (Exception ignore) {
            } finally {
                try { pout.close(); } catch (Exception ignore) {}
            }
        }, "s3-zip-writer").start();
        return pin;
    }

    @Override
    public org.springframework.http.MediaType guessMediaType(String mimeFromDb, String originalName) {
        // Local과 동일 로직 사용
        try {
            if (mimeFromDb != null && !mimeFromDb.isBlank()) {
                return org.springframework.http.MediaType.parseMediaType(mimeFromDb);
            }
        } catch (Exception ignore) {}
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
                : null;
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
