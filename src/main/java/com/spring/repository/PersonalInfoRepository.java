/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.spring.entity.PersonalInfo
 *  com.spring.entity.Profile
 *  com.spring.repository.PersonalInfoRepository
 *  org.springframework.data.jpa.repository.JpaRepository
 */
package com.spring.repository;

import com.spring.entity.PersonalInfo;
import com.spring.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalInfoRepository extends JpaRepository<PersonalInfo, Long> {
    public PersonalInfo findByProfile(Profile profile);
}

