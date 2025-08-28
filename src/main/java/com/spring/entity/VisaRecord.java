package com.spring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "visa_record",
    indexes = {
        @Index(name = "idx_agent", columnList = "agent_id")
    }
)
public class VisaRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visa_id")
    private Long visaId;

    // FK: admin.admin_id (ON DELETE SET NULL 은 DB 제약으로 동작)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "agent_id", foreignKey = @ForeignKey(name = "fk_visa_admin"))
    private Admin agent;   // Admin 엔티티에 adminId 필드가 있어야 함

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "company_address", length = 500)
    private String companyAddress;

    @Column(name = "company_contact", length = 100)
    private String companyContact;

    @Column(name = "headcount")
    private Integer headcount;

    @Column(name = "nationality", length = 100)
    private String nationality;

    @Column(name = "job_code", length = 50)
    private String jobCode;

    @Column(name = "worker_name", length = 100)
    private String workerName;

    @Column(name = "immigration_office", length = 200)
    private String immigrationOffice;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.RECEIVED; // 기본값: 비자접수중

    @Lob
    @Column(name = "remarks")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Builder.Default
    @Column(name = "visa_deleted", nullable = false)
    private Boolean visaDeleted = Boolean.FALSE;

}
