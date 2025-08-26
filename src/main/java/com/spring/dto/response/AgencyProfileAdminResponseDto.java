package com.spring.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.spring.entity.AgencyProfile;
import com.spring.entity.AgencyProfileFile;
import com.spring.entity.ProfileStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyProfileAdminResponseDto { // 최고권한 1,2 전용
    private Long profileId;
    private String agencyName;      // 관리자만 볼 수 있음
    private String visaType;
    private String jobCode;
    private String employeeNameEn;
    private String nationalityEn;

    private ProfileStatus status;   // READY / ASSIGNED
    private String statusLabel;     // "미배정" / "배정완료"

    private LocalDateTime createdAt;

    @Builder.Default
    private List<AgencyProfileFileResponseDto> files = Collections.emptyList(); // Java 8 호환

    /** Entity -> DTO 매핑 편의 메서드 */
    public static AgencyProfileAdminResponseDto fromEntity(AgencyProfile p) {
        return AgencyProfileAdminResponseDto.builder()
                .profileId(p.getProfileId())
                .agencyName(p.getAgencyName())
                .visaType(p.getVisaType())
                .jobCode(p.getJobCode())
                .employeeNameEn(p.getEmployeeNameEn())
                .nationalityEn(p.getNationalityEn())
                .status(p.getStatus())
                .statusLabel(p.getStatus() != null ? p.getStatus().getLabel() : "미배정")
                .createdAt(p.getCreatedAt())
                .files(p.getFiles() == null ? Collections.emptyList()
                        : p.getFiles().stream()
                              .map(AgencyProfileAdminResponseDto::toFileDto)
                              .collect(Collectors.toList()))
                .build();
    }

    private static AgencyProfileFileResponseDto toFileDto(AgencyProfileFile f) {
        return AgencyProfileFileResponseDto.builder()
                .fileId(f.getFileId())
                .originalName(f.getOriginalName())
                .storageKey(f.getStorageKey())
                .mimeType(f.getMimeType())
                .fileSize(f.getFileSize())
                .build();
    }
}
