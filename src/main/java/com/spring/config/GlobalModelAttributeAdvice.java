/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.spring.config.GlobalModelAttributeAdvice
 *  com.spring.entity.Admin
 *  jakarta.servlet.http.HttpSession
 *  org.springframework.ui.Model
 *  org.springframework.web.bind.annotation.ControllerAdvice
 *  org.springframework.web.bind.annotation.ModelAttribute
 */
package com.spring.config;

import com.spring.entity.Admin;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributeAdvice {

    @ModelAttribute
    public void addLoginInfo(Model model, HttpSession session) {
        Admin loginAdmin = (Admin) session.getAttribute("loggedInAdmin");

        boolean isAdmin = (loginAdmin != null);
        model.addAttribute("isAdmin", isAdmin);

        // 로그인 이름 (없으면 빈 문자열)
        model.addAttribute("adminName", isAdmin ? loginAdmin.getAdminName() : "");

        // 권한 ID (없으면 null)
        Integer authorityId = (isAdmin && loginAdmin.getAuthorityType() != null)
                ? loginAdmin.getAuthorityType().getAuthorityId()
                : null;
        model.addAttribute("authorityId", authorityId);

        // (선택) adminId도 전역 제공
        Long adminId = isAdmin ? loginAdmin.getAdminId() : null;
        model.addAttribute("adminId", adminId);
        
        // ✅ Visa 등록 가능 여부 전역 제공
        boolean canRegisterVisa = (loginAdmin != null)
                && loginAdmin.getAuthorityType() != null
                && (authorityId == 1 || authorityId == 2 || authorityId == 5);
        model.addAttribute("canRegisterVisa", canRegisterVisa);

    }
}

