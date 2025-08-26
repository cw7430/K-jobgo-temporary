package com.spring.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private AgencyProfile profile;  // N:1

    @Column(nullable = false, length = 255)
    private String originalName;    // 원본 파일명

    @Column(nullable = false, length = 500)
    private String storageKey;      // S3 키/경로

    @Column(length = 100)
    private String mimeType;        // MIME 타입

    private Long fileSize;          // 바이트 단위

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /* 선택 사항 — 필요해지면 주석 해제하여 스키마/엔티티 동시 확장
    @Column(length = 64)
    private String contentHash;     // SHA-256 등

    private Integer displayOrder;   // 정렬
    */
}
