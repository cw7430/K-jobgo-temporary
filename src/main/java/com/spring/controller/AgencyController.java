package com.spring.controller;

// import java.util.List;
import java.util.Locale;

import com.spring.dto.request.AgencyProfileRequestDto;
import com.spring.dto.response.AgencyRowDto;
import com.spring.service.AgencyProfileService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/agency")
public class AgencyController {

    @Autowired
    private AgencyProfileService profileService;

    // 1) 목록 페이지
    @GetMapping({"/List", "/agencyList"})
    public String showAgencyListPage(@RequestParam(required = false) String keyword,
                                     @PageableDefault(size = 10) Pageable pageable,
                                     HttpSession session,
                                     Model model) {

        Integer authorityId = (Integer) session.getAttribute("authorityId"); // 로그인 여부
        boolean isAdmin     = authorityId != null;
        boolean isSuper     = isAdmin && (authorityId == 1 || authorityId == 2);

        // ✅ 퍼블릭(미로그인)은 등록을 완료한 이력이 있어야만 접근 가능
        if (!isAdmin) {
            Boolean registeredOnce = (Boolean) session.getAttribute("registeredOnce");
            if (registeredOnce == null || !registeredOnce) {
                return "redirect:/agency/register";
            }
        }

        Page<AgencyRowDto> pageObj = profileService.findAgencyPage(keyword, pageable);

        model.addAttribute("pageObj", pageObj);
        model.addAttribute("keyword", keyword);
        model.addAttribute("authorityId", authorityId);
        model.addAttribute("adminName", session.getAttribute("adminName"));
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isSuper", isSuper);
        return "agency/agencyList";
    }

    // 등록 페이지 뷰 반환
    
    @GetMapping("/register")
    public String showAgencyRegisterPage() {
        return "agency/agencyRegister";  // 세션 체크 없이 항상 등록 페이지 열기
    }

    /*
    @GetMapping("/register")
    public String showAgencyRegisterPage(HttpSession session) {
        Integer authorityId = (Integer) session.getAttribute("authorityId");
        if (authorityId != null) {
            // 로그인(1,2,5)은 등록 페이지 비공개 → 현황으로 보내기
            return "redirect:/agency/List";
        }
        return "agency/agencyRegister";
    }
*/
    
    // 2) 등록 처리 (multipart/form-data) — 단일 파일 업로드 기본
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<?> registerAgency(
          @RequestParam String agencyName,
          @RequestParam String visaType,
          @RequestParam String jobCode,
          @RequestParam("employeeNameEn") String employeeName,
          @RequestParam("nationalityEn") String nationality,
          @RequestParam("file") MultipartFile file,   // ✅ 단일 파일
          // ※ 다중 업로드로 전환 시 아래 라인을 주석 해제하고 위 라인을 주석 처리
          // @RequestPart(value = "files", required = false) List<MultipartFile> files,
          HttpSession session
    ) {
        // (0) 권한 정책: Public(null) + 관리자(1,2) 허용, 5는 불가
        Integer authorityId = (Integer) session.getAttribute("authorityId");
        if (authorityId != null && !(authorityId == 1 || authorityId == 2))  {
            return ResponseEntity.status(403).body("등록 권한이 없습니다.");
        }

        // (1) 모든 항목 필수 검증
        if (!StringUtils.hasText(agencyName))   return ResponseEntity.badRequest().body("agencyName 필수");
        if (!StringUtils.hasText(visaType))     return ResponseEntity.badRequest().body("visaType 필수");
        if (!StringUtils.hasText(jobCode))      return ResponseEntity.badRequest().body("jobCode 필수");
        if (!StringUtils.hasText(employeeName)) return ResponseEntity.badRequest().body("employeeName 필수");
        if (!StringUtils.hasText(nationality))  return ResponseEntity.badRequest().body("nationality 필수");

        // (2) 파일 1개 필수 + 검증
        if (file == null || file.isEmpty()) return ResponseEntity.badRequest().body("이력서를 반드시 1개 업로드해야 합니다.");
        String fileError = validateFile(file);
        if (fileError != null) return ResponseEntity.badRequest().body(fileError);

        // ※ 다중 업로드 모드로 전환 시 (아래 주석 해제)
        /*
        boolean hasFile = files != null && files.stream().anyMatch(f -> f != null && !f.isEmpty());
        if (!hasFile) return ResponseEntity.badRequest().body("파일은 반드시 1개 이상 업로드해야 합니다.");
        String filesError = validateFiles(files);
        if (filesError != null) return ResponseEntity.badRequest().body(filesError);
        */

        // (3) DTO 변환 → 서비스 호출
        AgencyProfileRequestDto dto = AgencyProfileRequestDto.builder()
                .agencyName(agencyName)
                .visaType(visaType)
                .jobCode(jobCode)
                .employeeNameEn(employeeName)
                .nationalityEn(nationality)
                .file(file)     // ✅ 단일 파일
                // .files(files) // ← 다중 업로드 모드 시 사용
                .build();

        Long id = profileService.registerAgency(dto);

        // ✅ 등록 완료 플래그 → Public도 목록 접근 가능
        session.setAttribute("registeredOnce", true);

        // ✅ 저장 후 목록으로 이동 (프런트에서 location.href로 처리)
        return ResponseEntity.ok("/agency/List");
    }

    // 3) 배정/다운로드 처리 (권한 1,2,5)
    @GetMapping("/files/{profileId}")
    public ResponseEntity<?> downloadAndAssign(
            @PathVariable Long profileId,
            HttpSession session) {

        Integer authorityId  = (Integer) session.getAttribute("authorityId");
        Long currentAdminId  = (Long) session.getAttribute("adminId");

        if (authorityId == null || !(authorityId == 1 || authorityId == 2 || authorityId == 5)) {
            return ResponseEntity.status(403).body("다운로드 권한이 없습니다.");
        }

        // 1) 다운로드 직전 상태 '배정' 처리
        profileService.assignAndReturnStatus(profileId, authorityId, currentAdminId);

        // 2) 다운로드 패키지 생성 (단일 파일 정책)
        var pack = profileService.buildDownloadPackage(profileId); // AgencyDownloadPackage
        if (pack == null || pack.isEmpty()) {
            return ResponseEntity.status(404).body("첨부파일이 없습니다.");
        }

        // ✅ 단일 파일 정책: 항상 단일 파일명 사용
        String filename = pack.getSingleOriginalName();

        // ※ 다중 파일 + ZIP 모드로 전환 시 (아래 주석 해제)
        /*
        String filename = pack.isSingle()
                ? pack.getSingleOriginalName()
                : "agency_" + profileId + ".zip";
        */

        var cd = ContentDisposition.attachment().filename(filename).build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .contentType(pack.getMediaType())          // 단건은 실제 MIME, ZIP은 application/zip
                .contentLength(pack.getContentLength())    // 알 수 없으면 생략 가능
                .body(new InputStreamResource(pack.getInputStream()));
    }

    // 4) 삭제 (권한 1,2)
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteAgency(@PathVariable Long id, HttpSession session) {
        Integer authorityId = (Integer) session.getAttribute("authorityId");
        if (authorityId == null || !(authorityId == 1 || authorityId == 2)) {
            return ResponseEntity.status(403).body("삭제 권한이 없습니다.");
        }
        profileService.deleteById(id);
        return ResponseEntity.ok("deleted");
    }

    // ====== 검증 유틸 ======

    // ✅ 단일 파일 검증
    private static String validateFile(MultipartFile f) {
        if (f == null || f.isEmpty()) return "At least one file is required.";
        String original = f.getOriginalFilename();
        String ext = (original == null) ? "" : getExt(original).toLowerCase(Locale.ROOT);
        boolean allowed = ext.equals("pdf") || ext.equals("doc") || ext.equals("docx")
                       || ext.equals("xls") || ext.equals("xlsx")
                       || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png");
        if (!allowed) return "Unsupported file type: " + original;
        if (f.getSize() > 10L * 1024 * 1024) return "File too large (max 10MB): " + original;
        return null;
    }

    // ※ 다중 파일 검증 — 나중에 다중 업로드로 전환 시 사용
    /*
    private static String validateFiles(List<MultipartFile> files) {
        if (files == null) return "At least one file is required.";
        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) continue;
            String original = f.getOriginalFilename();
            String ext = (original == null) ? "" : getExt(original).toLowerCase(Locale.ROOT);
            boolean allowed = ext.equals("pdf") || ext.equals("doc") || ext.equals("docx")
                           || ext.equals("xls") || ext.equals("xlsx")
                           || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png");
            if (!allowed) return "Unsupported file type: " + original;
            if (f.getSize() > 10L * 1024 * 1024) return "File too large (max 10MB): " + original;
        }
        return null;
    }
    */

    private static String getExt(String filename) {
        int idx = filename.lastIndexOf('.');
        return (idx >= 0 && idx < filename.length() - 1) ? filename.substring(idx + 1) : "";
    }
}
