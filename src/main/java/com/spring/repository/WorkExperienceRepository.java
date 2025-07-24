/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.spring.entity.Profile
 *  com.spring.entity.WorkExperience
 *  com.spring.repository.WorkExperienceRepository
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Modifying
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.data.repository.query.Param
 *  org.springframework.transaction.annotation.Transactional
 */
package com.spring.repository;

import com.spring.entity.Profile;
import com.spring.entity.WorkExperience;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface WorkExperienceRepository
extends JpaRepository<WorkExperience, Long> {
    public List<WorkExperience> findByProfile(Profile var1);

    @Modifying
    @Transactional
    @Query(value="DELETE FROM WorkExperience w WHERE w.profile = :profile")
    public void deleteByProfile(@Param(value="profile") Profile profile);
}

