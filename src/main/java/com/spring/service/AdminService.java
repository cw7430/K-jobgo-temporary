/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.spring.entity.Admin
 *  com.spring.service.AdminService
 */
package com.spring.service;

import com.spring.entity.Admin;
import java.util.List;

public interface AdminService {
    public Admin authenticate(String adminLoginId, String adminPassword);

    public List<Admin> findAll();
}

