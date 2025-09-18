package com.spring.client.entity;

import com.spring.client.enums.ApprStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"contacts","attachments","approvalHistory"})
@Entity
@Table(name = "cmp_info")
public class CmpInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cmp_id")
    private Long cmpId;

    @Column(name = "cmp_name", nullable = false, length = 255)
    private String cmpName;

    @Column(name = "ceo_name", nullable = false, length = 100)
    private String ceoName;

    @Column(name = "biz_no", nullable = false, length = 20)
    private String bizNo;

    @Column(name = "biz_email", nullable = false, unique = true, length = 255)
    private String bizEmail;

    @Column(name = "biz_pwd", nullable = false, length = 255)
    private String bizPwd;

    @Column(name = "zip_code", nullable = false, length = 10)
    private String zipCode;

    @Column(name = "cmp_addr", nullable = false, length = 255)
    private String cmpAddr;

    @Column(name = "addr_dt", nullable = false, length = 255)
    private String addrDt;

    @Column(name = "cmp_phone", length = 50)
    private String cmpPhone;

    @Column(name = "agr_terms", nullable = false)
    private boolean agrTerms;

    @Column(name = "prx_join", nullable = false)
    private boolean prxJoin;

    @AssertTrue(message = "첨부파일 확인 동의 여부를 선택해 주세요.")
    @Column(name = "file_confirm", nullable = false)
    private boolean fileConfirm;

    @Column(name = "proxy_executor", length = 100)
    private String proxyExecutor;

    @Column(name = "is_del", nullable = false)
    private boolean isDel;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "del_dt")
    private LocalDateTime delDt;
    
    @Column(name = "must_change_pwd", nullable = false)
    private boolean mustChangePwd;  // 기본값 false

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "appr_status", nullable = false, length = 10)
    private ApprStatus apprStatus = ApprStatus.PENDING;

    @Column(name = "processed_by", length = 100)
    private String processedBy;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "crt_dt", nullable = false, updatable = false)
    private LocalDateTime crtDt;

    @Column(name = "upd_dt", nullable = false)
    private LocalDateTime updDt;

    @OneToMany(mappedBy = "cmpInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CmpCont> contacts;

    @OneToMany(mappedBy = "cmpInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CmpAttach> attachments;

    @OneToMany(mappedBy = "cmpInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CmpApprHist> approvalHistory;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.crtDt = now;
        this.updDt = now;
        if (this.apprStatus == null) {
            this.apprStatus = ApprStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updDt = LocalDateTime.now();
    }

    // 기존 (하위호환)
    public void approve() {
        this.apprStatus = ApprStatus.APPROVED;
        this.processedAt = LocalDateTime.now();
    }

    public void reject() {
        this.apprStatus = ApprStatus.REJECTED;
        this.processedAt = LocalDateTime.now();
    }
    
    // 승인 처리
    public void approve(String adminName) {
        this.apprStatus = ApprStatus.APPROVED;
        this.processedBy = adminName;
        this.processedAt = LocalDateTime.now();
    }

    // 반려 처리
    public void reject(String adminName) {
        this.apprStatus = ApprStatus.REJECTED;
        this.processedBy = adminName;
        this.processedAt = LocalDateTime.now();
    }
}
