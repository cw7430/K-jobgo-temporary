package com.spring.dto.response;

import com.spring.entity.AgencyProfile;
import com.spring.entity.AgencyProfileFile;
import com.spring.entity.ProfileStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyProfileDownloadResponseDto { // 권한 번호 5 행정사 전용
	   private Long profileId;
	    private String visaType;
	    private String jobCode;
	    private String employeeNameEn;
	    private String nationalityEn;

	    private ProfileStatus status;   // READY, ASSIGNED
	    private String statusLabel;     // "미배정" / "배정완료"

	    private LocalDateTime createdAt;

	    @Builder.Default
	    private List<AgencyProfileFileResponseDto> files = Collections.emptyList(); // Java 8 호환

	    /** Entity → DTO 매핑 편의 메서드 */
	    public static AgencyProfileDownloadResponseDto fromEntity(AgencyProfile p) {
	        return AgencyProfileDownloadResponseDto.builder()
	                .profileId(p.getProfileId())
	                .visaType(p.getVisaType())
	                .jobCode(p.getJobCode())
	                .employeeNameEn(p.getEmployeeNameEn())
	                .nationalityEn(p.getNationalityEn())
	                .status(p.getStatus())
	                .statusLabel(p.getStatus() != null ? p.getStatus().getLabel() : "미배정")
	                .createdAt(p.getCreatedAt())
	                .files(p.getFiles() == null ? Collections.emptyList()
	                        : p.getFiles().stream()
	                              .map(f -> AgencyProfileFileResponseDto.builder()
	                                      .fileId(f.getFileId())
	                                      .originalName(f.getOriginalName())
	                                      .storageKey(f.getStorageKey())
	                                      .mimeType(f.getMimeType())
	                                      .fileSize(f.getFileSize())
	                                      .build())
	                              .collect(Collectors.toList()))
	                .build();
	    }
	}
