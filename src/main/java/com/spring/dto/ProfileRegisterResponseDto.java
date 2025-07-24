package com.spring.dto;

import com.spring.dto.response.EducationAdminResponseDto;
import com.spring.dto.response.PersonalInfoAdminResponseDto;
import com.spring.dto.response.ProfileAdminResponseDto;
import com.spring.dto.response.WorkExperienceAdminResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRegisterResponseDto {

    private ProfileAdminResponseDto profile;
    private PersonalInfoAdminResponseDto personalInfo;
    private List<EducationAdminResponseDto> educationList;
    private List<WorkExperienceAdminResponseDto> workList;
    private String strengths;

}
