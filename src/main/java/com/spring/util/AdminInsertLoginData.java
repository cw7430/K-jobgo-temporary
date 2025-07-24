/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.spring.util.AdminInsertLoginData
 *  org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
 */
package com.spring.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AdminInsertLoginData {
    public static void main(String[] args) {
        String[][] admins;
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        for (String[] admin : admins = new String[][]{{"\uc774\uc218\ud658", "01033352541", "dltnghks6^", "010-3335-2541", "dltnghks27@naver.com", "\ubc29\ubb38\uc0c1\ub2f4", "\uc804\ubd81", "3"}}) {
            String encodedPassword = passwordEncoder.encode((CharSequence)admin[2]);
            System.out.println("INSERT INTO admin (admin_name, admin_login_id, admin_password, admin_phone, admin_email, job_duty, job_area, authority_id) VALUES ('" + admin[0] + "', '" + admin[1] + "', '" + encodedPassword + "', '" + admin[3] + "', '" + admin[4] + "', '" + admin[5] + "', '" + admin[6] + "', " + admin[7] + ");");
        }
    }
}

