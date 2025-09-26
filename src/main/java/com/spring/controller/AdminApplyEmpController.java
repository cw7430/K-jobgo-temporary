package com.spring.controller;

import com.spring.client.dto.ApplyEmpAdminLogDTO;
import com.spring.client.dto.ApplyEmpAdminLogViewDTO;
import com.spring.client.dto.CmpJobConditionDto;
import com.spring.client.entity.CmpJobCondition;
import com.spring.client.enums.JobStatus;
import com.spring.client.repository.ApplyEmpAdminLogRepository;
import com.spring.client.service.impl.ApplyEmpAdminLogService;
import com.spring.client.service.CmpJobConditionService;
import com.spring.entity.Admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/applyEmp")
public class AdminApplyEmpController {

    private final ApplyEmpAdminLogService logService;
    private final CmpJobConditionService jobConditionService;

    // --- ✅ 관리자 전용: 채용요청 목록/상세 ---
    // 보안: SecurityConfig에서 /admin/applyEmp/** 는 SUPERADMIN/ADMIN(또는 STAFF 등)에게만 허용
    @GetMapping("")
    public String adminApplyRedirect(
            @RequestParam(required=false) String status,
            @RequestParam(required=false) String q,
            @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size,
            @RequestParam(defaultValue="false") boolean includeDeleted,
            @RequestParam(defaultValue="false") boolean mine
    ) {
        String qs = org.springframework.web.util.UriComponentsBuilder.fromPath("/admin/applyEmp/list")
                .queryParam("status", status)
                .queryParam("q", q)
                .queryParam("from", from)
                .queryParam("to", to)
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("includeDeleted", includeDeleted)
                .queryParam("mine", mine)
                .build().toUriString();
        return "redirect:" + qs;
    }
    
    @GetMapping("/list")
    public String adminApplyList(
            @RequestParam(required=false) String q,
            @RequestParam(required=false) String bucket,
            @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue="false") boolean includeDeleted,
            @RequestParam(defaultValue="false") boolean mine,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size,
            HttpSession session,
            HttpServletRequest request,
            Model model
    ) {
        // 헤더 토글/배지에 필요한 로그인 관리자 정보
        Admin loginAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loginAdmin != null) {
            model.addAttribute("isAdmin", true);
            model.addAttribute("adminName", loginAdmin.getAdminName());
            model.addAttribute("authorityId", loginAdmin.getAuthorityType().getAuthorityId());
        } else {
            model.addAttribute("authorityId", 0);
        }

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "jobId"));

        // ‘내 담당만’ 필터가 켜진 경우 현재 관리자 기준으로 필터링하도록 id 전달(서비스 구현 필요)
        Long mineAdminId = (mine && loginAdmin != null) ? loginAdmin.getAdminId() : null;

        // ✅ bucket → 상태 목록 매핑
        List<JobStatus> statuses = switch (bucket == null ? "" : bucket) {
            case "IN_PROGRESS" -> List.of(
                JobStatus.ACTIVE, JobStatus.PENDING, JobStatus.IN_PROGRESS, JobStatus.ON_HOLD
            );
            case "COMPLETED" -> List.of(JobStatus.COMPLETED);
            case "CANCELLED_OR_REJECTED" -> List.of(JobStatus.CANCELLED, JobStatus.REJECTED);
            default -> null; // 전체
        };
        
        Page<CmpJobCondition> result = jobConditionService.searchForAdmin(
                (q == null ? "" : q.trim()),
                statuses,                 // 🔁 단일 status → 상태목록
                from, to,
                includeDeleted,
                mineAdminId,
                pageable
        );

     // ✅ null 방어: 빈 페이지로 대체
        if (result == null) {
            result = Page.empty(pageable);
        }
        
        model.addAttribute("applyList", result.getContent());
        model.addAttribute("pageObj", result);
        // ✅ 뷰에서 안내문/페이징에 쓰도록 bucket을 내려줌
        model.addAttribute("filterBucket", bucket);
        
        Map<String, String> params = new HashMap<>();
        params.put("q", (q == null ? "" : q));
        params.put("bucket", (bucket == null ? "" : bucket)); 
        params.put("from", (from == null ? "" : from.toString()));
        params.put("to", (to == null ? "" : to.toString()));
        params.put("includeDeleted", Boolean.toString(includeDeleted)); // "true"/"false"
        params.put("mine", Boolean.toString(mine));                     // "true"/"false"
        model.addAttribute("param", params);

        // ★ 현재 목록 URL(필터 포함) 세션에 저장
        String urlWithQuery = request.getRequestURI() + (request.getQueryString()!=null ? "?" + request.getQueryString() : "");
        session.setAttribute("APPLY_LIST_LAST_URL", urlWithQuery);
        
        return "admin/applyList"; // 관리자용 목록 HTML
    }
    
    /** 상세 + 상담 타임라인 */
    @GetMapping("/detail/{jobId}")
    public String detail(@PathVariable Long jobId,
			            @RequestParam(required=false) Integer year,
			            @RequestParam(required=false) Integer month,
			            @RequestParam(defaultValue="false") boolean includeDeleted,
			            @RequestParam(defaultValue="0") int page,
			            @RequestParam(defaultValue="10") int size,
                         HttpSession session,
                         Model model) {

        Admin loginAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loginAdmin != null) {
            model.addAttribute("isAdmin", true);
            model.addAttribute("adminName", loginAdmin.getAdminName());
            model.addAttribute("authorityId", loginAdmin.getAuthorityType().getAuthorityId());
        } else {
            model.addAttribute("authorityId", 0);
        }

        model.addAttribute("isAdmin", true);                 // ✅ body data-is-admin에서 사용
        // 1) 상세 데이터/타임라인/월별 카운트
        var job = jobConditionService.findById(jobId);
        
        // 현재 상태
        String currentStatusName = (job.getStatus() != null ? job.getStatus().name() : "");
        
        // 한글 라벨 맵
        Map<String, String> statusLabel = Arrays.stream(JobStatus.values())
            .collect(Collectors.toMap(
                JobStatus::name,
                JobStatus::getLabelKo,
                (a,b) -> a,
                LinkedHashMap::new
            ));
        

     // ✅ 전체 상태 목록 + 허용 전환 목록(현재 null이면 ACTIVE로 간주)
        model.addAttribute("allStatuses", Arrays.asList(JobStatus.values()));
        JobStatus from = (job.getStatus() == null ? JobStatus.ACTIVE : job.getStatus());
        Set<String> allowedStatusNames = from.nextAllowed().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
        model.addAttribute("allowedStatusNames", allowedStatusNames);

        model.addAttribute("currentStatusName", currentStatusName);
        model.addAttribute("statusLabel", statusLabel);
        model.addAttribute("allowedStatusNames", allowedStatusNames);
        model.addAttribute("currentStatusName", currentStatusName);
        model.addAttribute("statusLabel", statusLabel);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("logId")));
        var timeline = logService.getTimeline(jobId, year, month, includeDeleted, pageable);

        // ★ 여기: 프로젝션 타입 그대로 받습니다 (예: List<ApplyEmpAdminLogRepository.YmCount>)
        var ymCounts = logService.getYearMonthCounts(jobId);

        Map<Integer, Map<Integer, Long>> countsByYear = new java.util.LinkedHashMap<>();
        if (ymCounts != null) {
            for (var row : ymCounts) {
                countsByYear
                    .computeIfAbsent(row.getY(), k -> new java.util.HashMap<>())
                    .put(row.getM(), row.getCnt());
            }
        }
        
        // 2) 연도 셀렉트 옵션 만들기 (중복 제거 + 내림차순)
        // 연도 옵션: 프로젝션에서 바로 꺼내면 됩니다.
        List<Integer> yearOptions =
            (ymCounts == null) ? java.util.List.of()
            : ymCounts.stream()
                      .map(ApplyEmpAdminLogRepository.YmCount::getY)
                      .filter(java.util.Objects::nonNull)
                      .distinct()
                      .sorted(java.util.Comparator.reverseOrder())
                      .toList();

        // 3) 모델 바인딩
        String logsBase = "/admin/applyEmp/" + jobId + "/logs";
        
        // badge 상태 값 
        var nextStatuses = (job.getStatus() == null)
        		? java.util.List.<JobStatus>of()
                : job.getStatus().nextAllowed();
        model.addAttribute("nextStatuses", nextStatuses);

        model.addAttribute("job", job);
        model.addAttribute("timeline", timeline);
        model.addAttribute("includeDeleted", includeDeleted);
        model.addAttribute("countsByYear", countsByYear);
        model.addAttribute("ymCounts", ymCounts);

        model.addAttribute("yearOptions", yearOptions);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedMonth", month);

        model.addAttribute("adminLogForm", new ApplyEmpAdminLogDTO());
        model.addAttribute("logPostUrl", logsBase);
        model.addAttribute("logFilterUrl", logsBase);

        // ★ 목록 복귀 URL을 모델에 주입
        String listRedirect = (String) session.getAttribute("APPLY_LIST_LAST_URL");
        if (listRedirect == null || listRedirect.isBlank()) listRedirect = "/admin/applyEmp/list";
        model.addAttribute("listRedirect", listRedirect);
        
        return "client/applyEmp";
    }


    /** 상담 로그 저장 (폼/AJAX 공용) → 타임라인 tbody만 리턴 */
    @PostMapping(value = "/{jobId}/logs", produces = MediaType.TEXT_HTML_VALUE)
    public String addLog(@PathVariable Long jobId,
                         @ModelAttribute("adminLogForm") @Valid ApplyEmpAdminLogDTO form,
                         @RequestParam(required=false) JobStatus to,
                         HttpSession session, Model model,
                         @RequestParam(defaultValue="0") int page,
                         @RequestParam(defaultValue="10") int size) {

        Admin loginAdmin = (Admin) session.getAttribute("loggedInAdmin");
        Long adminId = (loginAdmin != null ? loginAdmin.getAdminId() : null);

        // 0) 현재 상태 미리 읽어둠
        var current = jobConditionService.findById(jobId).getStatus();

        // 1) 상태 변경이 필요하면 "먼저" 변경
        if (to != null && !Objects.equals(current, to)) {
            if (current != null && !current.nextAllowed().contains(to)) {
                throw new IllegalArgumentException("허용되지 않은 상태 전환입니다.");
            }
            jobConditionService.adminHandle(jobId, form.getHandledBy(), form.getReferenceNote(), to);

            // (선택) 같은 행에서 상태변경 사실도 보이게 상담내용 앞에 붙임
            String fromKo = (current == null ? "(미설정)" : current.getLabelKo());
            String toKo   = to.getLabelKo();
            form.setCounselContent(String.format("[상태변경] %s → %s\n%s", fromKo, toKo, form.getCounselContent()));
        }

        // 2) 상담 로그는 "한 번만" 기록 (이 시점의 job.status는 이미 to 임)
        logService.addLog(jobId, adminId, form);

        // ❌ 별도 상태변경 로그는 더 이상 남기지 않음
        // logService.addStatusChange(...);  // 제거

        Pageable pageable = PageRequest.of(page, size,
            Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("logId")));
        var timeline = logService.getTimeline(jobId, null, null, false, pageable);

        model.addAttribute("timeline", timeline);
        model.addAttribute("isAdmin", true);
        model.addAttribute("statusLabel", buildStatusLabelMap());
        return "fragments/applyEmpAdminLog :: timelineBody";
    }
    
    @GetMapping(value = "/{jobId}/logs", produces = MediaType.TEXT_HTML_VALUE )
    public String filterLogs(@PathVariable Long jobId,
				            @RequestParam(required = false) Integer year,     // ★ nullable
				            @RequestParam(required = false) Integer month,    // ★ nullable
				            @RequestParam(defaultValue="false") boolean includeDeleted, 
				            @RequestParam(defaultValue="0") int page,
				            @RequestParam(defaultValue="10") int size,
                             Model model) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("logId")));

        // year/month가 null이면 전체(삭제 제외) 타임라인
        var timeline = logService.getTimeline(jobId, year, month, includeDeleted, pageable);

        model.addAttribute("timeline", timeline);
        model.addAttribute("isAdmin", true);
        model.addAttribute("statusLabel", buildStatusLabelMap());
        return "fragments/applyEmpAdminLog :: timelineBody";
    }
    
    private Map<String, String> buildStatusLabelMap() {
        return Arrays.stream(JobStatus.values())
                .collect(Collectors.toMap(
                    JobStatus::name,
                    JobStatus::getLabelKo,
                    (a,b) -> a,
                    LinkedHashMap::new
                ));
    }

// 상태 변경 (간단 POST 예시)
    @PostMapping("/detail/{jobId}/status")
    public String updateStatus(@PathVariable Long jobId,
                               @RequestParam JobStatus to,
                               RedirectAttributes ra) {
        var job = jobConditionService.findEntity(jobId); // 엔티티/DTO 적절히
        var from = job.getStatus();

        boolean allowed = (from == null) ? (to == JobStatus.ACTIVE)
                : from.nextAllowed().contains(to);
        
		if (!allowed) {
		ra.addFlashAttribute("error", "허용되지 않은 상태 전환입니다.");
		return "redirect:/admin/applyEmp/detail/" + jobId;
		}
		
		if (from != to) {
		job.setStatus(to);
		jobConditionService.save(job);
		ra.addFlashAttribute("ok", (from==null ? "(미설정)" : from.getLabelKo())
		+ " → " + to.getLabelKo() + "로 변경했습니다.");
		}

        return "redirect:/admin/applyEmp/detail/" + jobId;
    }
    
    @PostMapping("/detail/{jobId}/logs/{logId}/delete")
    @ResponseBody
    public Map<String,Object> deleteLog(@PathVariable Long jobId,
                                        @PathVariable Long logId,
                                        @RequestParam(required = false) String reason,
                                        HttpSession session) {
        Admin login = (Admin) session.getAttribute("loggedInAdmin");
        Long adminId = (login != null ? login.getAdminId() : null);

        logService.softDelete(jobId, logId, adminId, reason);
        return Map.of("ok", true);
    }
}
