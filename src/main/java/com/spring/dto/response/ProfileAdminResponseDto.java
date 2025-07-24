package com.spring.dto.response;

import com.spring.entity.PersonalInfo;
import com.spring.entity.Profile;
import lombok.*;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileAdminResponseDto {

    private Long profileId;
    private String nameKor;
    private String nameOrigin;
    private String gender;
    private String visaType;
    private LocalDate visaExpire;
    private Boolean visaExtendable;
    private String emailId;
    private String emailDomain;
    private String phone1;
    private String phone2;
    private String phone3;
    private String address;
    private Boolean drivingLicense;
    private Boolean dormitory;
    private String photoUrl;
    private MultipartFile photo;
    private String strengths;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private PersonalInfoAdminResponseDto personalInfo;

    public static ProfileAdminResponseDto fromEntity(Profile profile, PersonalInfo personalInfo) {
        String rawPhone = profile.getPhone();
        String[] parts = rawPhone != null ? rawPhone.split("-") : new String[]{};

        return ProfileAdminResponseDto.builder()
                .profileId(profile.getProfileId())
                .nameKor(profile.getNameKor())
                .nameOrigin(profile.getNameOrigin())
                .gender(profile.getGender())
                .visaType(profile.getVisaType())
                .visaExpire(profile.getVisaExpire())
                .visaExtendable(profile.getVisaExtendable())
                .emailId(profile.getEmail() != null ? profile.getEmail().split("@")[0] : "")
                .emailDomain(profile.getEmail() != null ? profile.getEmail().split("@")[1] : "")
                .phone1(parts.length > 0 ? parts[0] : "")
                .phone2(parts.length > 1 ? parts[1] : "")
                .phone3(parts.length > 2 ? parts[2] : "")
                .address(profile.getAddress())
                .drivingLicense(profile.getDrivingLicense())
                .dormitory(profile.getDormitory())
                .photoUrl(profile.getPhotoUrl())
                .strengths(profile.getStrengths())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .personalInfo(PersonalInfoAdminResponseDto.fromEntity(personalInfo))
                .build();
    }
}
