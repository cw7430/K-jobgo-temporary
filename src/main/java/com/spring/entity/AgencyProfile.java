package com.spring.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "agency_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;

    @Column(nullable = false, length = 200)
    private String agencyName; // 송출업체명 (권한 1,2만 노출)

    @Column(nullable = false, length = 50)
    private String visaType;   // 비자타입 (Visa Type)

    @Column(nullable = false, length = 50)
    private String jobCode;    // 직무코드 (Job Code)

    @Column(nullable = false, length = 150)
    private String employeeNameEn; // 근로자 이름 (영문)

    @Column(nullable = false, length = 100)
    private String nationalityEn;  // 국적 (영문)

    @Enumerated(EnumType.STRING)  // enum 이름 그대로 DB에 저장 (ex: READY, ASSIGNED)
    @Column(nullable = false, length = 20)
    private ProfileStatus status = ProfileStatus.READY;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    // 연관관계: Profile 1 ↔ N Files
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AgencyProfileFile> files = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
