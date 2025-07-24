package com.spring.profile.masked.dto;

import com.spring.dto.response.user.EducationUserResponseDto;
import com.spring.dto.response.user.PersonalInfoUserResponseDto;
import com.spring.dto.response.user.WorkExperienceUserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaskedProfileDto {
    private Long profileId;
    private String photoUrl;
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
    private String strengths;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private PersonalInfoUserResponseDto personalInfo;
    private List<EducationUserResponseDto> educationList;
    private List<WorkExperienceUserResponseDto> workList;
}
