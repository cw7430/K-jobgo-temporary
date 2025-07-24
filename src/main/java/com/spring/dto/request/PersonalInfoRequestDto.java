package com.spring.dto.request;

import com.spring.entity.PersonalInfo;
import com.spring.entity.Profile;
import lombok.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalInfoRequestDto {

    private String nationality;
    private Integer age;
    private Integer height;
    private Integer weight;
    private String firstEntry;
    private String topikLevel;
    private String expectedSalary;
    private String desiredLocation;

    public PersonalInfo toEntity(Profile profile) {
        LocalDate parsedFirstEntry = null;
        if (this.firstEntry != null && !this.firstEntry.isBlank()) {
            parsedFirstEntry = LocalDate.parse(this.firstEntry, DateTimeFormatter.ISO_DATE);
        }

        return PersonalInfo.builder()
                .profile(profile)
                .nationality(this.nationality)
                .age(this.age)
                .height(this.height)
                .weight(this.weight)
                .firstEntry(parsedFirstEntry)  // ✅ null 가능
                .topikLevel(this.topikLevel)
                .expectedSalary(this.expectedSalary)
                .desiredLocation(this.desiredLocation)
                .build();
    }
}
