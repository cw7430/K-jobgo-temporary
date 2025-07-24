package com.spring.entity;

import com.spring.dto.request.PersonalInfoRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "personal_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long personalId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Column(length = 50)
    private String nationality;

    private Integer age;
    private Integer height;
    private Integer weight;

    private LocalDate firstEntry;

    @Column(length = 10)
    private String topikLevel;

    private String expectedSalary;

    @Column(length = 100)
    private String desiredLocation;

    // ✅ DTO로부터 엔티티 업데이트
    public void updateFromDto(PersonalInfoRequestDto dto) {
        this.nationality = dto.getNationality();
        this.age = dto.getAge();
        this.height = dto.getHeight();
        this.weight = dto.getWeight();

        // ✅ firstEntry null 또는 빈문자열 방어 처리
        if (dto.getFirstEntry() != null && !dto.getFirstEntry().isBlank()) {
            this.firstEntry = LocalDate.parse(dto.getFirstEntry(), DateTimeFormatter.ISO_DATE);
        } else {
            this.firstEntry = null; // 또는 유지하고 싶으면 this.firstEntry 그대로
        }

        this.topikLevel = dto.getTopikLevel();
        this.expectedSalary = dto.getExpectedSalary();
        this.desiredLocation = dto.getDesiredLocation();
    }
}
