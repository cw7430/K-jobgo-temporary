/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.spring.entity.Admin
 *  com.spring.repository.AdminRepository
 *  com.spring.service.AdminService
 *  com.spring.service.AdminServiceImpl
 *  jakarta.persistence.EntityManager
 *  jakarta.persistence.PersistenceContext
 *  lombok.Generated
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
 *  org.springframework.stereotype.Service
 */
package com.spring.service;

import com.spring.entity.Admin;
import com.spring.repository.AdminRepository;
import com.spring.service.AdminService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import lombok.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl
implements AdminService {
    private final AdminRepository adminRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public void printEncodedPassword() {
        String rawPassword = "00000";
        String encodedPassword = this.passwordEncoder.encode((CharSequence)rawPassword);
        System.out.println("\ud14c\uc2a4\ud2b8\uc6a9 '00000' \uc554\ud638\ud654 \uacb0\uacfc: " + encodedPassword);
    }

    public Admin authenticate(String adminLoginId, String adminPassword) {
        this.entityManager.clear();
        Admin admin = this.adminRepository.findByAdminLoginId(adminLoginId);
        if (admin != null) {
            System.out.println("\ub85c\uadf8\uc778 \uc2dc\ub3c4 adminId: " + admin.getAdminId());
            System.out.println("\ub85c\uadf8\uc778 \uc2dc\ub3c4 \uc774\ub984: " + admin.getAdminName());
            System.out.println("\uad8c\ud55c ID: " + admin.getAuthorityType().getAuthorityId());
            System.out.println("\uad8c\ud55c\uba85: " + admin.getAuthorityType().getAuthorityName());
            System.out.println("\uc785\ub825 Password: [" + adminPassword + "]");
            String dbPassword = admin.getAdminPassword().trim();
            System.out.println("DB Password: [" + dbPassword + "]");
            System.out.println("\uc694\uccad adminLoginId: " + adminLoginId);
            boolean match = this.passwordEncoder.matches((CharSequence)adminPassword, dbPassword);
            System.out.println("Password match result: " + match);
            if (dbPassword.startsWith("$2a$") ? this.passwordEncoder.matches((CharSequence)adminPassword, dbPassword) : dbPassword.equals(adminPassword)) {
                return admin;
            }
        }
        return null;
    }

    public List<Admin> findAll() {
        return this.adminRepository.findAll();
    }

    @Generated
    public AdminServiceImpl(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }
    
}

