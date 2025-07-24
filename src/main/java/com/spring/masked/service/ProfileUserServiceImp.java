package com.spring.masked.service;

import com.spring.dto.response.EducationAdminResponseDto;
import com.spring.dto.response.PersonalInfoAdminResponseDto;
import com.spring.dto.response.WorkExperienceAdminResponseDto;
import com.spring.dto.response.user.EducationUserResponseDto;
import com.spring.dto.response.user.PersonalInfoUserResponseDto;
import com.spring.dto.response.user.WorkExperienceUserResponseDto;
import com.spring.entity.Education;
import com.spring.entity.PersonalInfo;
import com.spring.entity.Profile;
import com.spring.entity.WorkExperience;
import com.spring.masked.service.ProfileUserService;
import com.spring.page.dto.ProfilePage;
import com.spring.profile.masked.dto.MaskedProfileDto;
import com.spring.repository.ProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileUserServiceImp implements ProfileUserService {

    private final ProfileRepository profileRepository;

    public ProfileUserServiceImp(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public ProfilePage getMaskedProfilePage(int page, int size, String desiredLocation, String nationality, String gender, String visaType, String keyword, String nationalityType, List<String> excludeNationalities) {
        PageRequest pageable = PageRequest.of(page - 1, size);

        if (desiredLocation != null && desiredLocation.isEmpty()) desiredLocation = null;
        if (nationality != null && nationality.isEmpty()) nationality = null;
        if (gender != null && gender.isEmpty()) gender = null;
        if (visaType != null && visaType.isEmpty()) visaType = null;
        if (keyword != null && keyword.isEmpty()) keyword = null;

        Page<Profile> filteredPage = profileRepository.findByFilters(
                desiredLocation, nationality, gender, visaType,
                keyword, nationalityType, excludeNationalities, pageable
        );

        List<MaskedProfileDto> maskedPageList = filteredPage.getContent()
                .stream()
                .map(this::toMaskedDto)
                .collect(Collectors.toList());

        ProfilePage profilePage = new ProfilePage();
        profilePage.setCurrentPage(page);
        profilePage.setPageSize(size);
        profilePage.setTotalItems((int) filteredPage.getTotalElements());
        profilePage.setProfileList(maskedPageList);

        int pageBlock = 10;
        int totalPages = filteredPage.getTotalPages();
        profilePage.setTotalPages(totalPages);
        profilePage.setStartPage((page - 1) / pageBlock * pageBlock + 1);
        profilePage.setEndPage(Math.min(profilePage.getStartPage() + pageBlock - 1, totalPages));

        return profilePage;
    }

    public MaskedProfileDto getMaskedProfileDetail(Long profileId) {
        Profile entity = profileRepository.findById(profileId).orElse(null);
        return entity != null ? this.toMaskedDto(entity) : null;
    }

    private MaskedProfileDto toMaskedDto(Profile profile) {
        MaskedProfileDto dto = new MaskedProfileDto();
        dto.setProfileId(profile.getProfileId());
        dto.setPhotoUrl(profile.getPhotoUrl());
        dto.setCreatedAt(profile.getCreatedAt());
        dto.setUpdatedAt(profile.getUpdatedAt());
        dto.setNameKor(maskName(profile.getNameKor()));
        dto.setNameOrigin(maskName(profile.getNameOrigin()));
        dto.setGender(profile.getGender());
        dto.setVisaType(profile.getVisaType());
        dto.setVisaExpire(profile.getVisaExpire());
        dto.setVisaExtendable(profile.getVisaExtendable());

        String email = profile.getEmail();
        if (email != null && email.contains("@")) {
            String[] emailParts = email.split("@");
            dto.setEmailId(maskEmail(emailParts[0]));
            dto.setEmailDomain(maskEmailDomain(emailParts[1]));
        } else {
            dto.setEmailId("");
            dto.setEmailDomain("");
        }

        String phone = profile.getPhone();
        if (phone != null && phone.split("-").length == 3) {
            String[] phoneParts = phone.split("-");
            dto.setPhone1(maskPhone(phoneParts[0]));
            dto.setPhone2(maskPhone(phoneParts[1]));
            dto.setPhone3(maskPhone(phoneParts[2]));
        } else {
            dto.setPhone1("");
            dto.setPhone2("");
            dto.setPhone3("");
        }

        dto.setAddress(maskAddress(profile.getAddress()));
        dto.setDrivingLicense(profile.getDrivingLicense());
        dto.setDormitory(profile.getDormitory());
        dto.setStrengths(profile.getStrengths());

        if (profile.getPersonalInfo() != null) {
            dto.setPersonalInfo(
                    PersonalInfoUserResponseDto.fromEntity(
                            PersonalInfoAdminResponseDto.fromEntity(profile.getPersonalInfo())
                    )
            );
        } else {
            dto.setPersonalInfo(null);
        }

        if (profile.getEducationList() != null) {
            dto.setEducationList(
                    profile.getEducationList()
                            .stream()
                            .map(e -> EducationUserResponseDto.fromEntity(
                                    EducationAdminResponseDto.fromEntity(e)))
                            .collect(Collectors.toList())
            );
        } else {
            dto.setEducationList(null);
        }

        if (profile.getWorkList() != null) {
            dto.setWorkList(
                    profile.getWorkList()
                            .stream()
                            .map(w -> WorkExperienceUserResponseDto.fromEntity(
                                    WorkExperienceAdminResponseDto.fromEntity(w)))
                            .collect(Collectors.toList())
            );
        } else {
            dto.setWorkList(null);
        }

        return dto;
    }

    private String maskName(String name) {
        if (name == null || name.length() < 2) return name;
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }

    private String maskEmail(String emailId) {
        if (emailId == null || emailId.length() < 2) return emailId;
        return emailId.charAt(0) + "**";
    }

    private String maskEmailDomain(String domain) {
        if (domain == null || domain.length() < 2) return domain;
        return "*".repeat(domain.length());
    }

    private String maskPhone(String phonePart) {
        if (phonePart == null || phonePart.length() < 2) return phonePart;
        return "*".repeat(phonePart.length());
    }

    private String maskAddress(String address) {
        if (address == null || address.length() < 4) return address;
        return address.substring(0, 2) + "****";
    }
}
