// src/main/java/com/spring/client/entity/ApplyEmpAdminLog.java
package com.spring.client.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.spring.client.enums.JobStatus;
import com.spring.entity.Admin;

@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor @Builder
@Entity
@Table(name = "apply_emp_admin_log",
       indexes = {
         @Index(name="ix_aelog_job_created", columnList = "job_id, created_at DESC, log_id DESC")
       })
public class ApplyEmpAdminLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private CmpJobCondition job; // FK → cmp_job_condition.job_id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_admin_id")
    private Admin writerAdmin;   // FK → admin.admin_id (이미 BIGINT)

    @Enumerated(EnumType.STRING) // ← 엔티티는 enum으로 다루되
    @Column(name="status_snapshot", length=32)
    private JobStatus statusSnapshot;     // DB는 VARCHAR에 enum name이 저장됨

    @Column(name = "writer_name", length = 100)
    private String writerName;   // 스냅샷

    @Column(name = "handled_by", length = 100)
    private String handledBy;

    @Lob
    @Column(name = "counsel_content")
    private String counselContent;

    @Lob
    @Column(name = "reference_note")
    private String referenceNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

 // 핵심 필드 이름은 "deleted" 프로퍼티를 쓰고, 칼럼명은 is_deleted 로 고정
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private Admin deletedBy;

    @Column(name = "delete_reason", columnDefinition = "TEXT")
    private String deleteReason;

    // 편의 메서드
    public void softDelete(Admin by, String reason) {
        if (this.deleted) return;
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = by;
        this.deleteReason = (reason != null && !reason.isBlank()) ? reason : null;
    }
  
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
