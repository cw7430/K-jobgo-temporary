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
	        model.addAttribute("adminName", isAdmin ? loginAdmin.getAdminName() : "");

	        Integer authorityId = (isAdmin && loginAdmin.getAuthorityType() != null)
	                ? loginAdmin.getAuthorityType().getAuthorityId()
	                : null;
	        model.addAttribute("authorityId", authorityId);

	        boolean isSuper = (authorityId != null) && (authorityId == 1 || authorityId == 2);
	        model.addAttribute("isSuper", isSuper);

	        // Public 등록 완료 플래그
	        Boolean registeredOnce = (Boolean) session.getAttribute("registeredOnce");

	        // 현황 메뉴: 1/2/5는 항상 true, Public은 등록 완료 시 true
	        boolean canListAgency =
	                (authorityId != null && (authorityId == 1 || authorityId == 2 || authorityId == 5))
	                || (authorityId == null && Boolean.TRUE.equals(registeredOnce));
	        model.addAttribute("canListAgency", canListAgency);

	        // 전용(등록) 메뉴: 오직 Public만 노출
	        boolean showAgencyRegister = (authorityId == null);
	        model.addAttribute("showAgencyRegister", showAgencyRegister);

	        // (기존) 비자 등록 메뉴
	        boolean canRegisterVisa = (authorityId != null) && (authorityId == 1 || authorityId == 2 || authorityId == 5);
	        model.addAttribute("canRegisterVisa", canRegisterVisa);

	        // (선택) adminId 전역 제공
	        model.addAttribute("adminId", isAdmin ? loginAdmin.getAdminId() : null);
	    }
}

