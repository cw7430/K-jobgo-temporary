/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.spring.entity.AuthorityType
 *  com.spring.repository.AuthorityTypeRepository
 *  org.springframework.data.jpa.repository.JpaRepository
 */
package com.spring.repository;

import com.spring.entity.AuthorityType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityTypeRepository
extends JpaRepository<AuthorityType, Integer> {
    public List<AuthorityType> findAll();
}

