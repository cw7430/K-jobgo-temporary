package com.spring.config;

import com.spring.client.entity.CmpInfo;
import com.spring.entity.Admin;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributeAdvice {
 
	 @ModelAttribute
	    public void addLoginInfo(Model model, HttpSession session) {

	        /* ===== 관리자 로그인 정보 ===== */
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

	        // 공개 등록 완료 플래그(옵션)
	        Boolean registeredOnce = (Boolean) session.getAttribute("registeredOnce");

	        // 송출업체 현황: 1/2/5는 항상 true, Public은 등록 완료 시 true
	        boolean canListAgency =
	                (authorityId != null && (authorityId == 1 || authorityId == 2 || authorityId == 5))
	                || (authorityId == null && Boolean.TRUE.equals(registeredOnce));
	        model.addAttribute("canListAgency", canListAgency);

	        // 송출업체 '등록' 메뉴: Public만 노출
	        boolean showAgencyRegister = (authorityId == null);
	        model.addAttribute("showAgencyRegister", showAgencyRegister);

	        // 비자 등록/관리: 1/2/5
	        boolean canRegisterVisa = (authorityId != null) && (authorityId == 1 || authorityId == 2 || authorityId == 5);
	        model.addAttribute("canRegisterVisa", canRegisterVisa);

	        // 국내 외국인 프로필: UI 노출 플래그(예: 3,6)
	        boolean canSeeProfiles = (authorityId != null) && (authorityId == 3 || authorityId == 6);
	        model.addAttribute("canSeeProfiles", canSeeProfiles);

	        // 회원관리 접근: 1/2/6
	        boolean confirmAgency = (authorityId != null) && (authorityId == 1 || authorityId == 2 || authorityId == 6);
	        model.addAttribute("confirmAgency", confirmAgency);

	        // 회원관리 접근: 1/2/6
	        boolean applyEmp = (authorityId != null) && (authorityId == 1 || authorityId == 2 || authorityId == 6);
	        model.addAttribute("applyEmp", applyEmp);
	        
	        // 헤더에서 회원관리 노출: 6만
	        boolean showConfirmInHeader = (authorityId != null) && authorityId == 6;
	        model.addAttribute("showConfirmInHeader", showConfirmInHeader);

	        // 관리자 ID(옵션)
	        model.addAttribute("adminId", isAdmin ? loginAdmin.getAdminId() : null);

	        /* ===== 기업(클라이언트) 로그인 정보 ===== */
	        Object clientObj = session.getAttribute("loggedInClient"); // 보통 CmpInfo
	        boolean isClient = (clientObj != null);
	        model.addAttribute("isClient", isClient);

	        String clientName = "";
	        Long clientCmpId = null;
	        if (isClient && clientObj instanceof CmpInfo cmp) {
	            clientName = cmp.getCmpName();
	            clientCmpId = cmp.getCmpId();
	        }
	        if (clientName.isEmpty()) {
	            Object nameAttr = session.getAttribute("clientName");
	            if (nameAttr != null) clientName = String.valueOf(nameAttr);
	        }

	        String myApprStatus = isClient
	                ? (String) session.getAttribute("clientApprStatus")
	                : null;

	        model.addAttribute("clientName", clientName);
	        model.addAttribute("clientCmpId", clientCmpId);
	        model.addAttribute("myApprStatus", myApprStatus);

	        /* ===== 공용/분리 플래그 ===== */
	        boolean isClientLoggedIn = isClient;
	        boolean isAdminLoggedIn  = isAdmin;
	        boolean isLoggedIn       = isAdmin || isClient;

	        model.addAttribute("isClientLoggedIn", isClientLoggedIn);
	        model.addAttribute("isAdminLoggedIn",  isAdminLoggedIn);
	        model.addAttribute("isLoggedIn",       isLoggedIn);

	        model.addAttribute("displayName", isAdmin ? model.getAttribute("adminName") : clientName);
	        
	        
	    }
	}