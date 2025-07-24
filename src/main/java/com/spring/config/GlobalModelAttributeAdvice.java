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
        Admin loginAdmin = (Admin)session.getAttribute("loggedInAdmin");
        model.addAttribute("isAdmin", (Object)(loginAdmin != null ? 1 : 0));
        model.addAttribute("adminName", (Object)(loginAdmin != null ? loginAdmin.getAdminName() : ""));
    }
}

