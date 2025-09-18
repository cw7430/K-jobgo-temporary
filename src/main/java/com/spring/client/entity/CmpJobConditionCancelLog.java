// src/main/java/com/spring/client/entity/CmpJobConditionCancelLog.java
package com.spring.client.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
@Entity
@Table(name = "cmp_job_condition_cancel_log")
public class CmpJobConditionCancelLog {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private CmpJobCondition job;

    @Column(name = "cancelled_at", nullable = false)
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by", length = 100)
    private String cancelledBy;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @PrePersist
    void onCreate() {
        if (cancelledAt == null) cancelledAt = LocalDateTime.now();
    }
}
