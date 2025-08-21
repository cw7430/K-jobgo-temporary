package com.spring.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agency_profile_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyProfileFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private AgencyProfile profile;  // 연관관계 N:1

    @Column(nullable = false, length = 255)
    private String originalName; // 원본 파일명

    @Column(nullable = false, length = 500)
    private String storageKey;   // S3 키 또는 경로

    @Column(length = 100)
    private String mimeType;     // MIME 타입 (pdf, word, image 등)

    private Long fileSize;       // 파일 크기 (byte 단위)

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
