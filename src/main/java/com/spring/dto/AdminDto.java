package com.spring.dto;

import com.spring.entity.Admin;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminDto {

    private Long adminId;
    private String adminName;
    private String adminLoginId;
    private String adminPassword;
    private String adminPhone;
    private String adminEmail;
    private String jobDuty;
    private String jobArea;
    private String authorityType;

    public AdminDto(Admin admin) {
        this.adminId = admin.getAdminId();
        this.adminName = admin.getAdminName();
        this.adminLoginId = admin.getAdminLoginId();
        this.adminPassword = admin.getAdminPassword();
        this.adminPhone = admin.getAdminPhone();
        this.adminEmail = admin.getAdminEmail();
        this.jobDuty = admin.getJobDuty();
        this.jobArea = admin.getJobArea();
        this.authorityType = admin.getAuthorityType().getAuthorityName();
    }
}
