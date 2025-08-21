package com.spring.controller;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.spring.dto.request.VisaRecordRequestDto;
import com.spring.entity.Admin;
import com.spring.entity.ApprovalStatus;
import com.spring.entity.VisaRecord;
import com.spring.repository.AdminRepository;
import com.spring.service.VisaRecordService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * VISA 관리 화면/액션 전담 컨트롤러.
 *
 * 요구사항 요약:
 * - 권한 5(에이전트/작성자)는 자신이 등록한 데이터만 CRUD 가능.
 * - 최고권한 1,2는 전체 데이터 조회(읽기) 가능.
 * - /admin/visa      : 전체 조회 화면(슈퍼는 전체, 일반은 서비스단에서 정책대로 제한)
 * - /admin/visa/my   : "내가 등록한 건"만 보는 마이페이지성 화면
 * - Ajax 엔드포인트  : 테이블 행 단위 등록/수정 후 HTML fragment 반환
 *
 * 보안 유의:
 * - 실제 쓰기 권한/소유자 검증은 Service에서 최종적으로 검사(중앙화).
 * - WebSecurity 설정에서 URL 접근권한을 별도로 제한하는 것을 권장.
 */

@Controller
@RequestMapping("/admin/visa")
@RequiredArgsConstructor
public class VisaController {

  private final VisaRecordService visaRecordService;
  private final AdminRepository adminRepository;

  /** 최고권한(1,2) 여부 판별 유틸 */
  private boolean isSuper(Admin admin) {
    if (admin == null || admin.getAuthorityType() == null) return false;
    Integer id = admin.getAuthorityType().getAuthorityId();
    return id != null && (id == 1 || id == 2);
  }

  /**
   * 모든 핸들러 진입 전 공통 모델 주입.
   * - 로그인 여부, 이름, 권한ID/이름, 슈퍼여부 등을 뷰에 전달
   * - 템플릿에서 th:if로 컬럼/툴바 노출 제어 가능
   */
  @ModelAttribute
  public void injectCurrentAdmin(Model model) {
    Admin admin = resolveCurrentAdminOrNull();
    model.addAttribute("isAdmin", admin != null);
    model.addAttribute("adminName", admin != null ? nullToEmpty(admin.getAdminName()) : "");

    Integer authorityId = (admin != null && admin.getAuthorityType() != null)
        ? admin.getAuthorityType().getAuthorityId()
        : null;
    String authorityName = (admin != null && admin.getAuthorityType() != null)
        ? admin.getAuthorityType().getAuthorityName()
        : null;

    model.addAttribute("authorityId", authorityId);
    model.addAttribute("authorityName", nullToEmpty(authorityName));
    model.addAttribute("isSuper", isSuper(admin));
  }

  /**
   * 메인 VISA 목록 화면.
   * - keyword / includeDeleted / page / size 파라미터로 페이징/검색
   * - partial=table 이면 tbody 조각만 반환(AJAX 부분 갱신용)
   * - 실제 데이터 가시성(전체/내 것)은 Service.search(...) 정책에 따름
   */
  @GetMapping({"", "/"})
  public String visaPage(
      @RequestParam(defaultValue = "")  String keyword,
      @RequestParam(defaultValue = "false") boolean includeDeleted,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false)   String partial,
      Model model) {

	  
	  Admin admin = resolveCurrentAdminOrNull();
	  if(admin != null && !isSuper(admin)) {
		  // 권한번호 5번 작성자 계정은 마이페이지로 이동
		    String q = org.springframework.web.util.UriComponentsBuilder
		            .fromPath("/admin/visa/my")
		            .queryParam("keyword", keyword)
		            .queryParam("includeDeleted", false) // 일반 계정은 삭제포함 강제 비활성
		            .queryParam("page", page)
		            .queryParam("size", size)
		            .build()
		            .encode(StandardCharsets.UTF_8) 
		            .toUriString();
		        return "redirect:" + q;
		      }
	  
	  // 최고권한 1,2 는 전체 조회
	  var pageable = PageRequest.of(Math.max(0, page), Math.max(1, size),
		      Sort.by(Sort.Direction.DESC, "visaId"));
	  var result = visaRecordService.search(keyword, includeDeleted, pageable);

    model.addAttribute("records", result.getContent());
    model.addAttribute("pageObj", result);
    model.addAttribute("keyword", (keyword == null) ? "" : keyword.trim());
    model.addAttribute("includeDeleted", includeDeleted);
    model.addAttribute("statuses", ApprovalStatus.values());
    model.addAttribute("registerForm", new VisaRecordRequestDto());
    model.addAttribute("mode", "create");

 // 부분 갱신 모드면 tbody 프래그먼트만 반환
    if ("table".equalsIgnoreCase(partial)) {
      return "visa/visaRegister :: tbodyRows";
    }
    return "visa/visaRegister";
  }

  /** 구버전 등록 URL 사용 시 메인으로 리다이렉트 */
  @GetMapping("/register")
  public String legacyRegisterRedirect() {
    return "redirect:/admin/visa";
  }

  /**
   * 동기 등록(폼 제출).
   * - 유효성 오류 시 최근 리스트와 함께 같은 화면 재렌더
   * - 정상 등록 시 목록으로 리다이렉트
   * - 실제 권한/소유자 설정은 Service.register(...)에서 처리
   */
  @PostMapping
  public String visaRegister(
      @ModelAttribute("registerForm") @Valid VisaRecordRequestDto form,
      BindingResult binding,
      Model model) {
    if (binding.hasErrors()) {
      model.addAttribute("records", visaRecordService.findRecent(200));
      model.addAttribute("statuses", ApprovalStatus.values());
      model.addAttribute("mode", "create");
      return "visa/visaRegister";
    }
    Long id = visaRecordService.register(form);
    return "redirect:/admin/visa";
  }

  /**
   * 동기 수정(폼 제출).
   * - Service.update(...)에서 권한5 + 소유자만 수정 허용 검사
   */
  @PostMapping("/{id}")
  public String visaUpdate(
      @PathVariable Long id,
      @ModelAttribute("registerForm") @Valid VisaRecordRequestDto form,
      BindingResult binding,
      Model model) {
    if (binding.hasErrors()) {
      model.addAttribute("records", visaRecordService.findRecent(200));
      model.addAttribute("statuses", ApprovalStatus.values());
      model.addAttribute("mode", "edit");
      return "visa/visaRegister";
    }
    visaRecordService.update(id, form);
    return "redirect:/admin/visa";
  }

  /**
   * 내 목록 전용 화면(마이페이지).
   * - 서비스의 searchMine(...)을 사용하여 로그인 사용자가 agent인 데이터만 조회
   * - 슈퍼(1,2)는 정책상 전체 조회로 fallback 하도록 구현 가능(서비스 쪽에서 처리)
   * - 템플릿은 메인과 동일 재사용
   */
  @GetMapping("/my")
  public String myVisaPage(@RequestParam(defaultValue="") String keyword,
                           @RequestParam(defaultValue="false") boolean includeDeleted,
                           @RequestParam(defaultValue="0") int page,
                           @RequestParam(defaultValue="20") int size,
                           Model model) {
      var pageable = PageRequest.of(Math.max(0,page), Math.max(1,size), Sort.by(Sort.Direction.DESC, "visaId"));
      var result = visaRecordService.searchMine(keyword, includeDeleted, pageable);
      model.addAttribute("records", result.getContent());
      model.addAttribute("pageObj", result);
      model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
      model.addAttribute("includeDeleted", includeDeleted);
      model.addAttribute("statuses", ApprovalStatus.values());
      model.addAttribute("registerForm", new VisaRecordRequestDto());
      model.addAttribute("mode", "create");
      return "visa/visaRegister";
  }

  /**
   * AJAX 등록: 저장 후 갱신된 <tr> 조각 반환.
   * - 프런트에서 현재 행을 서버 조각으로 교체
   * - 유효성 오류는 400으로 반환
   */
  @PostMapping(value = "/ajax", produces = "text/html; charset=UTF-8")
  @ResponseStatus(HttpStatus.OK)
  public String createAjax(
      @ModelAttribute("registerForm") @Valid VisaRecordRequestDto form,
      BindingResult binding,
      Model model) {
    if (binding.hasErrors()) {
      throw new org.springframework.web.server.ResponseStatusException(
          HttpStatus.BAD_REQUEST, "유효성 오류");
    }
    Long id = visaRecordService.register(form);
    VisaRecord saved = visaRecordService.getEntity(id);
    model.addAttribute("r", saved);
    return "fragments/visaFragments :: row(r=${r})";
  }

  /**
   * AJAX 수정: 저장 후 갱신된 <tr> 조각 반환.
   * - Service.update(...)에서 권한/소유자 검증
   */
  @PostMapping(value = "/{id}/ajax", produces = "text/html; charset=UTF-8")
  @ResponseStatus(HttpStatus.OK)
  public String updateAjax(
      @PathVariable Long id,
      @ModelAttribute("registerForm") @Valid VisaRecordRequestDto form,
      BindingResult binding,
      Model model) {
    if (binding.hasErrors()) {
      throw new org.springframework.web.server.ResponseStatusException(
          HttpStatus.BAD_REQUEST, "유효성 오류");
    }
    visaRecordService.update(id, form);
    VisaRecord updated = visaRecordService.getEntity(id);
    model.addAttribute("r", updated);
    return "fragments/visaFragments :: row(r=${r})";
  }

  /**
   * 삭제(소프트 딜리트).
   * - Service.delete(...)에서 권한5 + 소유자만 삭제 허용 검사
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> visaDeleted(@PathVariable Long id) {
    visaRecordService.delete(id);
    return ResponseEntity.noContent().build();
  }

  /** 현재 로그인한 Admin 엔티티 조회(없으면 null) */
  private Admin resolveCurrentAdminOrNull() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) return null;

    Object p = auth.getPrincipal();
    if (p instanceof Admin a) return a;

    String loginId = Objects.toString(p, null);
    if (loginId == null) return null;
    return adminRepository.findByAdminLoginId(loginId);
  }

  /** null-safe 문자열 변환 */
  private String nullToEmpty(String s) { return (s == null) ? "" : s; }
}
