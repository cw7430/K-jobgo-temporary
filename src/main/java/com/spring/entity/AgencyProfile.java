package com.spring.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    @Column(name="profile_id")
    private Long profileId;

    @Column(nullable = false, length = 200)
    private String agencyName; // 송출업체명 (권한 1,2만 노출)

    @Column(nullable = false, length = 50)
    private String visaType;   // 비자타입

    @Column(nullable = false, length = 50)
    private String jobCode;    // 직무코드 (ISCO 4자리)

    @Column(nullable = false, length = 150)
    private String employeeNameEn; // 근로자 이름

    @Column(nullable = false, length = 100)
    private String nationalityEn;  // 국적

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProfileStatus status = ProfileStatus.READY;

    @Builder.Default
    @Column(name="is_deleted", nullable=false)
    private boolean deleted = false;

    // 운영 추적
    @Column(name = "created_by_admin_id")
    private Long createdByAdminId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 연관관계: Profile 1 ↔ N Files
    @Builder.Default
    @OneToMany(mappedBy = "profile",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<AgencyProfileFile> files = new ArrayList<>();

    /** 양방향 고정 헬퍼 */
    public void addFile(AgencyProfileFile f) {
        if (f == null) return;
        f.setProfile(this);
        this.files.add(f);
    }

    public void removeFile(AgencyProfileFile f) {
        if (f == null) return;
        f.setProfile(null);
        this.files.remove(f);
    }
}