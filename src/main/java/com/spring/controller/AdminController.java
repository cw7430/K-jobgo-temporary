package com.spring.controller;

import com.spring.client.dto.ConfirmRowDto;
import com.spring.client.entity.CmpAttach;
import com.spring.client.enums.ApprStatus;
import com.spring.client.repository.CmpAttachRepository;
import com.spring.client.repository.CmpInfoRepository;
import com.spring.client.service.CmpJobConditionService;
import com.spring.client.service.ConfirmClientService;
import com.spring.config.SecurityConfig;
import com.spring.dto.ProfileRegisterRequestDto;
import com.spring.dto.ProfileRegisterResponseDto;
import com.spring.dto.request.PersonalInfoRequestDto;
import com.spring.entity.Admin;
import com.spring.page.dto.ProfilePage;
import com.spring.service.FileService;
import com.spring.service.ProfileService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
// 국내외국인프로필, 회원가입 접수 처리 관련 페이지 컨트롤러
public class AdminController {

    private final SecurityConfig securityConfig;
    private final ProfileService profileService;
    private final FileService fileService;
    
    private final CmpInfoRepository cmpInfoRepository;
    private final CmpAttachRepository cmpAttachRepository;
    private final ConfirmClientService confirmClientService;

    
    @GetMapping("/adminMain")            
    public String adminMain() {
        return "admin/adminMain";   // templates/admin/adminMain.html
    }

    @GetMapping("/admin/profileList")
    public String profileList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String desiredLocation,
            @RequestParam(required = false) String nationality,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String visaType,
            @RequestParam(required = false) String keyword,
            Model model) {

        String nationalityType = null;
        List<String> excludeNationalities = List.of();

        if ("기타".equals(nationality)) {
            nationalityType = "etc";
            excludeNationalities = List.of(
                    "네팔", "방글라데시", "미얀마", "몽골", "베트남",
                    "스리랑카", "우즈베키스탄", "인도", "인도네시아",
                    "캄보디아", "키르기스스탄", "파키스탄", "필리핀"
            );
            nationality = null;
        } else if (nationality != null && !nationality.isBlank()) {
            nationalityType = null;
        } else {
            nationality = null;
            nationalityType = null;
        }

        ProfilePage pageInfo = profileService.getFileProfilePage(
                page, desiredLocation, nationality, gender, visaType, keyword,
                nationalityType, excludeNationalities
        );

        model.addAttribute("isAdmin", true);
        model.addAttribute("profileList", pageInfo.getProfiles());
        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("desiredLocation", desiredLocation);
        model.addAttribute("nationality", nationality);
        model.addAttribute("gender", gender);
        model.addAttribute("visaType", visaType);
        model.addAttribute("keyword", keyword);

        return "adminProfile/profileList";
    }

    @GetMapping("/admin/profileRegister")
    public String showRegisterForm(Model model) {
        model.addAttribute("profileDto", new ProfileRegisterRequestDto());
        return "adminProfile/profileRegister";
    }

    @PostMapping("/admin/profileRegister")
    public String profileRegister(@ModelAttribute ProfileRegisterRequestDto profileDto,
                                  @RequestParam MultipartFile photo) throws IOException {
        PersonalInfoRequestDto info = profileDto.getPersonalInfo();

        System.out.println("=== ✅ personalInfo 객체 확인 ===");
        if (info != null) {
            System.out.println("  국적: " + info.getNationality());
            System.out.println("  나이: " + info.getAge());
            System.out.println("  키: " + info.getHeight());
            System.out.println("  몸무게: " + info.getWeight());
            System.out.println("  최초입국일: " + info.getFirstEntry());
            System.out.println("  TOPIK: " + info.getTopikLevel());
            System.out.println("  희망 급여: " + info.getExpectedSalary());
            System.out.println("  희망 근무지: " + info.getDesiredLocation());
        } else {
            System.out.println("❌ personalInfo is null!");
        }

        if (photo != null && !photo.isEmpty()) {
            String photoUrl = fileService.upload(photo);
            profileDto.getProfile().setPhotoUrl(photoUrl);
        }

        Long profileId = profileService.saveProfile(profileDto);
        return "redirect:/admin/profileDetail/" + profileId;
    }

    @GetMapping("/admin/profileDetail/{id}")
    public String profileDetail(@PathVariable Long id,
                                @RequestParam(required = false, defaultValue = "1") Integer page,
                                @RequestParam(required = false) String desiredLocation,
                                @RequestParam(required = false) String nationality,
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String visaType,
                                @RequestParam(required = false) String keyword,
                                HttpSession session,
                                Model model) {

        ProfileRegisterResponseDto profileDto = profileService.getProfileForUpdate(id);
        if (profileDto == null || profileDto.getProfile() == null) {
            return "redirect:/admin/profileList";
        }

        model.addAttribute("profileDto", profileDto);
        model.addAttribute("desiredLocation", desiredLocation);
        model.addAttribute("nationality", nationality);
        model.addAttribute("gender", gender);
        model.addAttribute("visaType", visaType);
        model.addAttribute("keyword", keyword);

        ProfilePage pageInfo = new ProfilePage();
        pageInfo.setCurrentPage(page != null ? page : 1);
        model.addAttribute("pageInfo", pageInfo);

        Admin loginAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loginAdmin != null) {
            model.addAttribute("isAdmin", true);
            model.addAttribute("adminName", loginAdmin.getAdminName());
            model.addAttribute("authorityId", loginAdmin.getAuthorityType().getAuthorityId());
        } else {
            model.addAttribute("authorityId", 0);
        }

        return "adminProfile/profileDetail";
    }

    @GetMapping("/admin/profileUpdate/{id}")
    public String showUpdateForm(@PathVariable Long id,
                                 HttpSession session,
                                 Model model) {
        ProfileRegisterResponseDto profileDto = profileService.getProfileForUpdate(id);
        if (profileDto == null || profileDto.getProfile() == null) {
            return "redirect:/admin/profileList";
        }

        model.addAttribute("profileDto", profileDto);

        Admin loginAdmin = (Admin) session.getAttribute("loggedInAdmin");
        if (loginAdmin != null) {
            model.addAttribute("adminName", loginAdmin.getAdminName());
            model.addAttribute("authorityId", loginAdmin.getAuthorityType().getAuthorityId());
        } else {
            model.addAttribute("authorityId", 0);
        }

        return "adminProfile/profileUpdate"; // ✅ 수정 폼 화면
    }

    @PostMapping("/admin/profileUpdate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProfile(
            @ModelAttribute ProfileRegisterRequestDto profileDto,
            @RequestParam(value = "photo", required = false) MultipartFile photo) throws IOException {

        if (photo != null && !photo.isEmpty()) {
            String photoUrl = fileService.upload(photo);
            profileDto.getProfile().setPhotoUrl(photoUrl);
        }

        profileService.updateProfile(profileDto);

        // ✅ JSON 반환: 상태 + 리다이렉트 경로 포함
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("redirectUrl", "/admin/profileDetail/" + profileDto.getProfile().getProfileId());

        return ResponseEntity.ok(response);
    }


    @PostMapping("/admin/profileDelete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteProfile(@PathVariable Long id) {
        try {
            profileService.deleteById(id);
            return ResponseEntity.ok("삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제 실패: " + e.getMessage());
        }
    }
    
    // 회원관리페이지 매핑 /templates/admin/confirmClient.html
    @GetMapping("/admin/confirmClient")
    public String confirmClient(
            @RequestParam(defaultValue = "PENDING") ApprStatus status,

            // 🔸 필터 파라미터
            @RequestParam(required = false) String field,        // all | cmpName | bizNo | contactName | contactPhone | proxyExecutor
            @RequestParam(required = false) String keyword,      // 텍스트
            @RequestParam(required = false) String prxJoinVal,   // "true" (대리만) | null/"" (전체)
            @RequestParam(required = false) String dateType, // "created" | "processed"
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,

            HttpSession session,
            Model model
    ) {
        // ... (헤더/페이지네이션 동일)
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));

        // ✅ 서비스 호출 (신규)
        Page<ConfirmRowDto> result = confirmClientService.searchFiltered(
                status, field, keyword, prxJoinVal, dateType, dateFrom, dateTo, pageable
        );

        model.addAttribute("companies", result.getContent());
        model.addAttribute("pageObj", result);
        model.addAttribute("filterStatus", status);

        // ✅ 화면 유지용 새 파라미터만 바인딩
        model.addAttribute("field", field);
        model.addAttribute("keyword", keyword);
        model.addAttribute("prxJoinVal", prxJoinVal);
        model.addAttribute("dateType", dateType);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo",   dateTo);

        // 탭 카운트
        model.addAttribute("pendingCount",  cmpInfoRepository.countByApprStatusAndIsDelFalse(ApprStatus.PENDING));
        model.addAttribute("approvedCount", cmpInfoRepository.countByApprStatusAndIsDelFalse(ApprStatus.APPROVED));
        model.addAttribute("rejectedCount", cmpInfoRepository.countByApprStatusAndIsDelFalse(ApprStatus.REJECTED));

        return "admin/confirmClient";
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    
    /** 공용: 경로가 URL인지(local path인지) 판단 */
    private boolean isRemote(String path) {
        return path != null && (path.startsWith("http://") || path.startsWith("https://"));
    }

    /** 미리보기(이미지/PDF는 브라우저 내장뷰어로) */
    @GetMapping("/admin/files/{id}/preview")
    public ResponseEntity<?> previewFile(@PathVariable Long id) throws Exception {
        CmpAttach file = cmpAttachRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다. id=" + id));

        // S3 등 원격이면 302 redirect
        if (isRemote(file.getFPath())) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(file.getFPath()))
                    .build();
        }

        Path p = Paths.get(file.getFPath());
        byte[] bytes = Files.readAllBytes(p);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getFMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getOrigName() + "\"")
                .body(new ByteArrayResource(bytes));
    }

    /** 직접 다운로드 */
    @GetMapping("/admin/files/{id}/download")
    public ResponseEntity<?> downloadFile(@PathVariable Long id) throws Exception {
        CmpAttach file = cmpAttachRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다. id=" + id));

        if (isRemote(file.getFPath())) {
            // 원격이면 브라우저가 직접 다운로드 하도록 리다이렉트
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(file.getFPath()))
                    .build();
        }

        Path p = Paths.get(file.getFPath());
        byte[] bytes = Files.readAllBytes(p);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getOrigName() + "\"")
                .body(new ByteArrayResource(bytes));
    }

    /** 기업별 첨부 ZIP 다운로드 (BUSINESS_LICENSE / BUSINESS_CARD 묶음) */
    @GetMapping("/admin/confirmClient/files/{cmpId}/downloadAll")
    @Transactional(readOnly = true)
    public void downloadAllFiles(@PathVariable Long cmpId, HttpServletResponse resp) throws Exception {
        var files = cmpAttachRepository.findByCmpInfo_CmpId(cmpId);
        String nowStr = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String zipName = "attachments_" + cmpId + "_" + nowStr + ".zip";

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/zip");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + zipName + "\"");

        try (ZipOutputStream zos = new ZipOutputStream(resp.getOutputStream())) {
            for (CmpAttach f : files) {
                byte[] data;

                if (isRemote(f.getFPath())) {
                    try (var in = new URL(f.getFPath()).openStream()) {
                        data = in.readAllBytes();
                    }
                } else {
                    data = Files.readAllBytes(Paths.get(f.getFPath()));
                }

                String entryName = (f.getFileCategory() != null ? f.getFileCategory().name() + "_" : "")
                        + f.getOrigName();
                zos.putNextEntry(new ZipEntry(entryName));
                zos.write(data);
                zos.closeEntry();
            }
            zos.finish();
        }
    }
    
 // 단건 저장
    @PostMapping("/admin/confirmClient/save")
    @ResponseBody
    public ResponseEntity<?> saveOne(@RequestBody DecisionDto dto, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            confirmClientService.applyDecision(
                    dto.getCmpId(),
                    dto.getStatus(),
                    dto.getRejectReason(),
                    admin.getAdminName(),
                    dto.isSendEmail()
            );
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 일괄 저장
    @PostMapping("/admin/confirmClient/saveBatch")
    @ResponseBody
    public ResponseEntity<?> saveBatch(@RequestBody List<DecisionDto> dtos, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            var commands = dtos.stream()
                    .map(d -> new ConfirmClientService.DecisionCommand(
                            d.getCmpId(), d.getStatus(), d.getRejectReason(), d.isSendEmail()))
                    .toList();
            confirmClientService.applyBatch(commands, admin.getAdminName());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    /** POST 바디용 DTO */
    public static class DecisionDto {
        private Long cmpId;
        private ApprStatus status;   // "PENDING" | "APPROVED" | "REJECTED"
        private String rejectReason; // 반려시에만 필수
        private boolean sendEmail;   // 메일 발송할지 여부

        public Long getCmpId() { return cmpId; }
        public ApprStatus getStatus() { return status; }
        public String getRejectReason() { return rejectReason; }
        public boolean isSendEmail() { return sendEmail; }

        public void setCmpId(Long cmpId) { this.cmpId = cmpId; }
        public void setStatus(ApprStatus status) { this.status = status; }
        public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
        public void setSendEmail(boolean sendEmail) { this.sendEmail = sendEmail; }
    }
    
 // 행 인라인 편집 저장 (회사명/담당자/연락처 + (반려상태라면) 반려사유)
    @PostMapping("/admin/confirmClient/inlineEdit")
    @ResponseBody
    public ResponseEntity<?> inlineEdit(@RequestBody InlineEditDto dto, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            confirmClientService.inlineEdit(
                dto.getCmpId(),
                dto.getCmpName(),
                dto.getContactName(),
                dto.getContactPhone(),
                dto.getRejectReason(),
                admin.getAdminName()
            );
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /** 인라인 편집 저장용 DTO */
    public static class InlineEditDto {
        private Long cmpId;
        private String cmpName;
        private String contactName;
        private String contactPhone;
        private String rejectReason;

        public Long getCmpId() { return cmpId; }
        public String getCmpName() { return cmpName; }
        public String getContactName() { return contactName; }
        public String getContactPhone() { return contactPhone; }
        public String getRejectReason() { return rejectReason; }

        public void setCmpId(Long v) { this.cmpId = v; }
        public void setCmpName(String v) { this.cmpName = v; }
        public void setContactName(String v) { this.contactName = v; }
        public void setContactPhone(String v) { this.contactPhone = v; }
        public void setRejectReason(String v) { this.rejectReason = v; }
    }
}
