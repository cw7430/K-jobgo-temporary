package com.spring.controller;

import com.spring.client.dto.request.JoinRequestDTO;
import com.spring.client.dto.CmpJobConditionDto;
import com.spring.client.dto.request.ApplyEmpForm;
import com.spring.client.entity.CmpInfo;
import com.spring.client.entity.CmpJobCondition;
import com.spring.client.enums.JobStatus;
import com.spring.client.repository.CmpInfoRepository;
import com.spring.client.service.CmpJobConditionService;
import com.spring.client.service.EmailService;
import com.spring.client.service.JoinService;
import com.spring.util.BizNoUtils;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
 * - 마이페이지 (GET/POST)
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
    private final CmpInfoRepository cmpInfoRepository;

   
    @GetMapping("/login")
    public String loginAlias(@RequestParam(required = false) String redirect,
                             org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
      if (redirect != null && !redirect.isBlank()) ra.addAttribute("redirect", redirect);
      return "redirect:/loginPage";
    }
    
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
                             RedirectAttributes ra, Model model) {
  
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

    /** ✅ 고객용 구직신청 목록 (공용 프래그먼트 렌더링) */
    @GetMapping("/client/applyEmp/list")
    public String clientApplyList(
            @RequestParam(required=false) String q,
            @RequestParam(required=false) JobStatus status,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size,
            HttpSession session,
            Model model
    ) {
        // 로그인/승인 가드
        CmpInfo cli = (CmpInfo) session.getAttribute("loggedInClient");
        if (cli == null) return "redirect:/loginPage";
        String appr = (String) session.getAttribute("clientApprStatus");
        if (appr == null || !"APPROVED".equals(appr)) return "redirect:/my/join-status";

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "jobId"));

        Page<CmpJobCondition> result = jobConditionService.searchForClient(
                cli.getCmpId(),
                (q == null ? "" : q.trim()),
                status,
                pageable
        );

        model.addAttribute("applyList", result.getContent());
        model.addAttribute("pageObj", result);
        model.addAttribute("filterStatus", status);
        // 프래그먼트/페이징 링크에서 그대로 쓰는 param 바인딩
        model.addAttribute("param", Map.of(
                "q", q,
                "status", status == null ? null : status.name()
        ));

        // 헤더 토글 등에 쓰는 플래그(선택)
        model.addAttribute("isClient", true);

        return "client/applyEmpList"; // ✅ 너가 만든 고객용 목록 HTML
    }

    /** ✅ 고객용 상세 (공용 상세 템플릿 사용) */
    @GetMapping("/client/applyEmp/detail/{id}")
    public String clientApplyDetail(@PathVariable Long id, HttpSession session, Model model) {
        CmpInfo cli = (CmpInfo) session.getAttribute("loggedInClient");
        if (cli == null) return "redirect:/loginPage";

        // 1) 소유권 검증 + 엔티티 조회
        CmpJobCondition entity = jobConditionService.loadByIdForCompany(id, cli.getCmpId());

        // 2) 공용 상세 템플릿 규격에 맞게 DTO로 바인딩
        CmpJobConditionDto job = CmpJobConditionDto.from(entity);
        model.addAttribute("job", job);

        // 3) 헤더/토글 플래그
        model.addAttribute("isAdmin", false);
        model.addAttribute("isClient", true);

        // 4) 공용 상세 템플릿 사용 (관리자/사용자 겸용)
        return "applyEmp";
    }
    /**
     * 구직요청 작성/수정 폼
     * @param newForm true면 기존 요청 상태와 무관하게 새 폼(초안)으로 진입
     * 폼 화면에도 헤더 토글만 추가
     */
    @GetMapping("/client/applyEmp")
    public String applyEmpForm(HttpSession session,
                               Model model,
                               CsrfToken csrfToken,
                               @RequestParam(name = "new", required = false, defaultValue = "false") boolean newForm,
                               @RequestParam(name = "readonly", required = false) String readonlyFlag) {

        CmpInfo cli = (CmpInfo) session.getAttribute("loggedInClient");
        if (cli == null) return "redirect:/loginPage";
        String appr = (String) session.getAttribute("clientApprStatus");
        if (appr == null || !"APPROVED".equals(appr)) return "redirect:/my/join-status";

        CmpJobCondition form;
        boolean readOnly;

        if (newForm) {
            form = new CmpJobCondition();
            form.setStatus(JobStatus.ACTIVE);
            readOnly = false;
        } else {
            form = jobConditionService.loadDraftOrNew(cli.getCmpId());
            readOnly = (form.getJobId() != null) && (form.getStatus() != JobStatus.ACTIVE);
        }

        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("form", form);
        model.addAttribute("readOnly", readOnly);
        if (readOnly || "1".equals(readonlyFlag)) {
            model.addAttribute("message", "관리자가 처리하여 이 요청은 더 이상 수정할 수 없습니다. '새 요청'으로 작성해 주세요.");
        }

        // 🔽 헤더 토글만 추가
        model.addAttribute("isAdmin", false);
        model.addAttribute("isClient", true);

        return "client/applyEmp"; // 폼 템플릿(입력용)
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
    
    /*
    // 기업 마이페이지    */
    @GetMapping("/client/clientMyPage")
    public String clientMyPage(HttpSession session, Model model, CsrfToken csrfToken) {
        CmpInfo cmp = (CmpInfo) session.getAttribute("loggedInClient");
        if (cmp == null) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String email = auth.getName();
                cmp = cmpInfoRepository.findByBizEmail(email).orElse(null);
                if (cmp != null) {
                    session.setAttribute("loggedInClient", cmp);
                    session.setAttribute("clientApprStatus", cmp.getApprStatus() != null ? cmp.getApprStatus().name() : null);
                    session.setAttribute("clientName", cmp.getCmpName());
                    session.setAttribute("clientCmpId", cmp.getCmpId());
                }
            }
        }
        if (cmp == null) return "redirect:/loginPage";

        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("client", cmp);
        return "client/clientMyPage";
    }

    // 승인 현황
    @GetMapping("/my/join-status")
    public String myJoinStatus(HttpSession session, Model model, CsrfToken csrfToken) {
        CmpInfo me = (CmpInfo) session.getAttribute("loggedInClient");
        if (me == null) return "redirect:/loginPage";

        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("client", me);

        Object statusAttr = session.getAttribute("clientApprStatus");
        String status = (statusAttr == null) ? "NONE" : String.valueOf(statusAttr);
        model.addAttribute("status", status);

        return "client/join-status"; // templates/client/join-status.html
    }

    /*
    // (옵션) 설정 페이지
    @GetMapping("/my/settings")
    public String mySettings(HttpSession session, Model model, CsrfToken csrfToken) {
        CmpInfo me = (CmpInfo) session.getAttribute("loggedInClient");
        if (me == null) return "redirect:/loginPage";
        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("client", me);
        return "client/settings"; // 필요 시 생성
    }
*/

}
