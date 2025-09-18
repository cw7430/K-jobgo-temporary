package com.spring.client.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cmp_cont")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmpCont {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long empId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cmp_id", nullable = false)
    private CmpInfo cmpInfo;

    @Column(name = "emp_name", nullable = false, length = 100)
    private String empName;

    @Column(name = "emp_title", nullable = false, length = 100)
    private String empTitle;

    @Column(name = "emp_phone", nullable = false, length = 50)
    private String empPhone;

    @Column(name = "crt_dt", nullable = false, updatable = false)
    private LocalDateTime crtDt;

    @Column(name = "upd_dt", nullable = false)
    private LocalDateTime updDt;

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.crtDt = now;
        this.updDt = now;
    }

    @PreUpdate
    private void onUpdate() {
        this.updDt = LocalDateTime.now();
    }
}