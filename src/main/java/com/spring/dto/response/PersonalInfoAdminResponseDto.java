package com.spring.dto.response;

import com.spring.entity.PersonalInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalInfoAdminResponseDto {

    private Long personalId;
    private String nationality;
    private Integer age;
    private Integer height;
    private Integer weight;
    private LocalDate firstEntry;
    private String topikLevel;
    private String expectedSalary;
    private String desiredLocation;

    public static PersonalInfoAdminResponseDto fromEntity(PersonalInfo personalInfo) {
        return PersonalInfoAdminResponseDto.builder()
                .personalId(personalInfo.getPersonalId())
                .nationality(personalInfo.getNationality())
                .age(personalInfo.getAge())
                .height(personalInfo.getHeight())
                .weight(personalInfo.getWeight())
                .firstEntry(personalInfo.getFirstEntry())
                .topikLevel(personalInfo.getTopikLevel())
                .expectedSalary(personalInfo.getExpectedSalary())
                .desiredLocation(personalInfo.getDesiredLocation())
                .build();
    }
}
