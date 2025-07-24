package com.spring.controller;

import com.spring.config.SecurityConfig;
import com.spring.dto.ProfileRegisterRequestDto;
import com.spring.dto.ProfileRegisterResponseDto;
import com.spring.dto.request.PersonalInfoRequestDto;
import com.spring.entity.Admin;
import com.spring.page.dto.ProfilePage;
import com.spring.service.FileService;
import com.spring.service.ProfileService;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AdminController {

    private final SecurityConfig securityConfig;
    private final ProfileService profileService;
    private final FileService fileService;

    public AdminController(SecurityConfig securityConfig, ProfileService profileService, FileService fileService) {
        this.securityConfig = securityConfig;
        this.profileService = profileService;
        this.fileService = fileService;
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
}
