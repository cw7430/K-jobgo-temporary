package com.spring.dto.response.user;

import com.spring.dto.request.PersonalInfoRequestDto;
import com.spring.dto.request.ProfileRequestDto;
import com.spring.dto.response.PersonalInfoAdminResponseDto;
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
public class ProfileUserResponseDto {
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

    // 정적 팩토리 메서드
    public static ProfileUserResponseDto fromEntity(ProfileRequestDto profileDto, PersonalInfoRequestDto personalInfoDto, Profile profileEntity) {
        PersonalInfo personalInfoEntity = null;
        if (personalInfoDto != null) {
            personalInfoEntity = personalInfoDto.toEntity(profileEntity);
        }

        return ProfileUserResponseDto.builder()
                .profileId(profileDto.getProfileId())
                .nameKor(profileDto.getNameKor())
                .nameOrigin(profileDto.getNameOrigin())
                .gender(profileDto.getGender())
                .visaType(profileDto.getVisaType())
                .visaExpire(profileDto.getVisaExpire())
                .visaExtendable(profileDto.getVisaExtendable())
                .emailId(profileDto.getEmailId())
                .emailDomain(profileDto.getEmailDomain())
                .phone1(profileDto.getPhone1())
                .phone2(profileDto.getPhone2())
                .phone3(profileDto.getPhone3())
                .address(profileDto.getAddress())
                .drivingLicense(profileDto.getDrivingLicense())
                .dormitory(profileDto.getDormitory())
                .photoUrl(profileDto.getPhotoUrl())
                .photo(profileDto.getPhoto())
                .strengths(profileDto.getStrengths())
                .createdAt(null)
                .updatedAt(null)
                .personalInfo(personalInfoDto != null ? PersonalInfoAdminResponseDto.fromEntity(personalInfoEntity) : null)
                .build();
    }
}
