package com.spring.controller;

import com.spring.client.dto.request.JoinRequestDTO;
import com.spring.client.dto.request.ApplyEmpForm;
import com.spring.client.entity.CmpInfo;
import com.spring.client.entity.CmpJobCondition;
import com.spring.client.enums.JobStatus;
import com.spring.client.service.CmpJobConditionService;
import com.spring.client.service.EmailService;
import com.spring.client.service.JoinService;
import com.spring.util.BizNoUtils;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 기업 회원용 컨트롤러
 * - 회원가입 (GET/POST)
 * - 가입 성공 안내 (GET)
 * - 구직요청 작성/수정 (GET/POST)
 *
 * 접근 제어:
 * - SecurityConfig 에서 /client/applyEmp/** 는 ROLE_COMPANY 로 제한
 * - ApprovedCompanyInterceptor 로 'APPROVED' 아닌 경우 /my/join-status 로 리다이렉트 권장
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ClientController {

    private final JoinService joinService;
    private final EmailService emailService;                  // (사용 계획 없으면 제거 가능)
    private final CmpJobConditionService jobConditionService; // 구인조건 도메인 서비스

    /** 회원가입 페이지 */
    @GetMapping("/client/joinPage")
    public String showClientJoinPage(Model model, CsrfToken csrfToken) {
        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("joinReq", new JoinRequestDTO());
        return "client/join"; // templates/client/join.html
    }

    /** 회원가입 제출 */
    @PostMapping("/join")
    public String submitJoin(@ModelAttribute("joinReq") @Valid JoinRequestDTO req,
                             BindingResult br,
                             RedirectAttributes ra) {
        if (br.hasErrors()) {
        	log.warn("[JOIN] hasErrors -> client/join");
            return "client/join";
        }

        // 1) 포맷 표준화
        String normalized = BizNoUtils.normalize(req.getBizNo());
        req.setBizNo(normalized);

        // 2) 사전 중복 체크
        if (joinService.existsBizNo(normalized)) {
        	log.warn("[JOIN] dup bizNo");
            br.rejectValue("bizNo", "dup", "이미 가입된 사업자등록번호입니다.");
            return "client/join";
        }
        if (joinService.existsEmail(req.getBizEmail())) {
            br.rejectValue("bizEmail", "dup", "이미 사용 중인 이메일입니다.");
            return "client/join";
        }

        // 3) 저장 + PRG
        try {
            Long cmpId = joinService.register(req);
            log.info("[JOIN] OK -> redirect, cmpId={}", cmpId);
            ra.addFlashAttribute("cmpName", req.getCmpName());
            ra.addFlashAttribute("cmpId", cmpId);
            return "redirect:/client/join-success";
        } catch (DataIntegrityViolationException e) {
        	log.error("[JOIN] DataIntegrityViolation", e);
            // 레이스 상황 등 최종 방어
            br.reject("dup", "이미 가입된 정보가 있습니다.");
            return "client/join";
        } catch (Exception e) {
        	log.error("[JOIN] Exception", e);
            br.reject("server", "처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return "client/join";
        }
    }

    /** 회원가입 신청(확인) 페이지 */
    @GetMapping("/client/join-success")
    public String joinSuccess() {
        return "client/join-success"; // templates/client/join-success.html
    }


    /* --------------------------------------------------------------------
     * ✅ 회원(기업) 전용: 구직요청 작성/수정
     *  - 상태: 관리자 처리(COMPLETED/CANCELLED 등)면 읽기 전용
     *  - ?new=1 로 진입 시 항상 "새 요청" 작성 모드로 렌더링
     * -------------------------------------------------------------------- */

    /**
     * 구직요청 작성/수정 폼
     * @param newForm true면 기존 요청 상태와 무관하게 새 폼(초안)으로 진입
     */
    @GetMapping("/client/applyEmp")
    public String applyEmpForm(HttpSession session,
                               Model model,
                               CsrfToken csrfToken,
                               @RequestParam(name = "new", required = false, defaultValue = "false") boolean newForm,
                               @RequestParam(name = "readonly", required = false) String readonlyFlag) {

        // 세션 가드 (인터셉터가 있다면 이 가드는 중복이지만, 안전망 차원에서 유지)
        CmpInfo cli = (CmpInfo) session.getAttribute("loggedInClient");
        if (cli == null) {
            return "redirect:/loginPage";
        }
        String appr = (String) session.getAttribute("clientApprStatus");
        if (appr == null || !"APPROVED".equals(appr)) {
            return "redirect:/my/join-status";
        }

        // 1) 폼 데이터 준비
        CmpJobCondition form;
        boolean readOnly;

        if (newForm) {
            // 새 요청 작성 모드(저장 전이므로 transient 객체)
            form = new CmpJobCondition();
            form.setStatus(JobStatus.ACTIVE);
            readOnly = false;
        } else {
            // 최신 요청(초안 우선, 없으면 새 객체) 로드
            form = jobConditionService.loadDraftOrNew(cli.getCmpId());
            // 관리자 처리/취소 등 ACTIVE가 아니면 읽기 전용으로 렌더링
            readOnly = (form.getJobId() != null) && (form.getStatus() != JobStatus.ACTIVE);
        }

        // 2) 모델 속성
        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("form", form);              // 화면에서 th:object 등으로 바인딩
        model.addAttribute("readOnly", readOnly);      // true면 input/textarea disable 처리
        if (readOnly || "1".equals(readonlyFlag)) {
            model.addAttribute("message", "관리자가 처리하여 이 요청은 더 이상 수정할 수 없습니다. '새 요청'으로 작성해 주세요.");
        }

        return "client/applyEmp"; // templates/client/applyEmp.html
    }

    /**
     * 구직요청 제출/저장
     * - 서버에서도 상태를 다시 확인하여 관리자 동시처리 상황을 안전하게 막는다.
     */
    @PostMapping("/client/applyEmp")
    public String submitApplyEmp(@ModelAttribute("form") @Valid ApplyEmpForm form,
                                 BindingResult binding,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        if (binding.hasErrors()) {
            return "client/applyEmp";
        }

        // 세션 가드
        CmpInfo cli = (CmpInfo) session.getAttribute("loggedInClient");
        if (cli == null) {
            return "redirect:/loginPage";
        }
        String appr = (String) session.getAttribute("clientApprStatus");
        if (appr == null || !"APPROVED".equals(appr)) {
            return "redirect:/my/join-status";
        }

        // 1) 저장 전에 상태 재점검
        try {
            // (선택) 기존 요청을 수정하려는 경우, ACTIVE가 아니면 차단
            if (form.getJobId() != null) {
                var current = jobConditionService.findById(form.getJobId());
                if (current.getStatus() != JobStatus.ACTIVE) {
                    // 이미 관리자에 의해 처리됨 → 읽기 전용 안내 + 새 요청 권유
                    ra.addFlashAttribute("message", "관리자가 처리하여 수정할 수 없습니다. '새 요청'으로 작성해 주세요.");
                    return "redirect:/client/applyEmp?readonly=1";
                }
            }

            // 2) 저장/제출
            jobConditionService.saveOrSubmit(cli.getCmpId(), form);
            ra.addFlashAttribute("toast", "구직요청이 저장되었습니다.");
            return "redirect:/client/applyEmp?success=1";

        } catch (IllegalStateException ex) {
            // 서비스에서 동시성/상태 위반을 IllegalStateException 으로 던지는 경우 처리
            log.warn("[APPLY-EMP] 상태 위반: {}", ex.getMessage());
            ra.addFlashAttribute("message", "요청 처리 중 상태가 변경되었습니다. '새 요청'으로 작성해 주세요.");
            return "redirect:/client/applyEmp?readonly=1";

        } catch (Exception e) {
            log.error("[APPLY-EMP] 저장 실패", e);
            ra.addFlashAttribute("message", "처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
            return "redirect:/client/applyEmp";
        }
    }
    
 // 기업 마이페이지
    @GetMapping("/client/clientMyPage")
    public String clientMyPage(HttpSession session, Model model, CsrfToken csrfToken) {
        model.addAttribute("_csrf", csrfToken);
        CmpInfo myPage = (CmpInfo) session.getAttribute("loggedInClient");
        if (myPage == null) {
            return "redirect:/loginPage";
        }
        model.addAttribute("client", myPage);
        return "client/clientMyPage"; // templates/client/clientMyPage.html 준비
    }

}
