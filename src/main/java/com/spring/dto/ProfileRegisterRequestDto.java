package com.spring.dto;

import com.spring.dto.request.EducationRequestDto;
import com.spring.dto.request.PersonalInfoRequestDto;
import com.spring.dto.request.ProfileRequestDto;
import com.spring.dto.request.WorkExperienceRequestDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ProfileRegisterRequestDto {

    private ProfileRequestDto profile;
    private PersonalInfoRequestDto personalInfo;
    private List<EducationRequestDto> educationList;
    private List<WorkExperienceRequestDto> workList;
    private String strengths;

}
