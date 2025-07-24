package com.spring.dto.response.user;

import com.spring.dto.response.PersonalInfoAdminResponseDto;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalInfoUserResponseDto {
    private Long personalId;
    private String nationality;
    private Integer age;
    private Integer height;
    private Integer weight;
    private LocalDate firstEntry;
    private String topikLevel;
    private String expectedSalary;
    private String desiredLocation;

    public static PersonalInfoUserResponseDto fromEntity(PersonalInfoAdminResponseDto personalAdminResponseDto) {
        return PersonalInfoUserResponseDto.builder()
                .personalId(personalAdminResponseDto.getPersonalId())
                .nationality(personalAdminResponseDto.getNationality())
                .age(personalAdminResponseDto.getAge())
                .height(personalAdminResponseDto.getHeight())
                .weight(personalAdminResponseDto.getWeight())
                .firstEntry(personalAdminResponseDto.getFirstEntry())
                .topikLevel(personalAdminResponseDto.getTopikLevel())
                .expectedSalary(personalAdminResponseDto.getExpectedSalary())
                .desiredLocation(personalAdminResponseDto.getDesiredLocation())
                .build();
    }
}
