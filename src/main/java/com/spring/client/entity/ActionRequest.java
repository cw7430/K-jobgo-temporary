// src/main/java/com/spring/client/entity/ActionRequest.java
package com.spring.client.entity;

import com.spring.client.enums.ApprStatus;
import com.spring.client.enums.ReqType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"cmp", "job"})
@Entity
@Table(name = "action_request")
public class ActionRequest {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "req_id")
    private Long reqId;

    @Enumerated(STRING)
    @Column(name = "req_type", nullable = false, length = 20)
    private ReqType reqType; // WITHDRAW, JOB_CANCEL

    @Builder.Default
    @Enumerated(STRING)
    @Column(name = "req_status", nullable = false, length = 20)
    private ApprStatus reqStatus = ApprStatus.PENDING; // PENDING, APPROVED, REJECTED

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "cmp_id", nullable = false)
    private CmpInfo cmp;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "job_id")
    private CmpJobCondition job; // JOB_CANCEL일 때만 설정

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "requested_by", length = 100)
    private String requestedBy;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "decided_by", length = 100)
    private String decidedBy;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @Column(name = "decision_note", columnDefinition = "TEXT")
    private String decisionNote;

    // DB 생성 칼럼(유니크 키) → 읽기전용 매핑
    @Column(name = "pending_key", insertable = false, updatable = false)
    private String pendingKey;

    @PrePersist
    void onCreate() {
        if (requestedAt == null) requestedAt = LocalDateTime.now();
        if (reqStatus == null) reqStatus = ApprStatus.PENDING;
    }
}
