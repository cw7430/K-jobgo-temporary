package com.spring.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "admin_name", nullable = false)
    private String adminName;

    @Column(name = "admin_login_id", nullable = false, unique = true)
    private String adminLoginId;

    @Column(name = "admin_password", nullable = false)
    private String adminPassword;

    @Column(name = "admin_phone")
    private String adminPhone;

    @Column(name = "admin_email", unique = true)
    private String adminEmail;

    @Column(name = "job_duty", nullable = false)
    private String jobDuty;

    @Column(name = "job_area", nullable = false)
    private String jobArea;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "authority_id", nullable = false)
    private AuthorityType authorityType;
}
