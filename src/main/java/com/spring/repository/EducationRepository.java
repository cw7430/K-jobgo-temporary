/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.spring.entity.Education
 *  com.spring.entity.Profile
 *  com.spring.repository.EducationRepository
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Modifying
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.data.repository.query.Param
 *  org.springframework.transaction.annotation.Transactional
 */
package com.spring.repository;

import com.spring.entity.Education;
import com.spring.entity.Profile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface EducationRepository
extends JpaRepository<Education, Long> {
    public List<Education> findByProfile(Profile var1);

    @Modifying
    @Transactional
    @Query(value="DELETE FROM Education e WHERE e.profile = :profile")
    public void deleteByProfile(@Param(value="profile") Profile profile);
}

