package com.spring.dto.request;

import com.spring.entity.ApprovalStatus;
import com.spring.entity.VisaRecord;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 비자 레코드 등록/수정 공용 DTO
 * - 등록/수정 모두 같은 필드 세트 사용
 * - approvalStatus: 등록에서도 select 활성화 → @NotNull
 * - remarks(비고)만 선택값
 * - agentId는 서버에서 인증 주체(Admin)로 세팅 (DTO에 포함하지 않음)
 */
@Data
public class VisaRecordRequestDto {

    // 회사 정보
    @NotBlank(message = "회사명은 필수입니다.")
    private String companyName;

    @NotBlank(message = "회사 주소는 필수입니다.")
    private String companyAddress;

    @NotBlank(message = "회사 담당자는 필수입니다.")
    private String companyContact;

    @NotNull(message = "인원 수는 필수입니다.")
    @Min(value = 1, message = "인원 수는 1명 이상이어야 합니다.")
    private Integer headcount;

    // 근로자/직무
    @NotBlank(message = "국적은 필수입니다.")
    private String nationality;

    @NotBlank(message = "직무 코드는 필수입니다.")
    private String jobCode;

    @NotBlank(message = "외국인 근로자 이름은 필수입니다.")
    private String workerName;

    // 접수 정보
    @NotBlank(message = "출입국 사무소는 필수입니다.")
    private String immigrationOffice;

    @NotNull(message = "접수 일자는 필수입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")  // <input type="date"> 바인딩용
    private LocalDate applicationDate;

    // 승인 상태 (등록 폼에서도 select 활성화하므로 필수)
    @NotNull(message = "승인 상태는 필수입니다.")
    private ApprovalStatus approvalStatus;

    // 선택값
    private String remarks;
    
    // Entity → DTO : 조회용
    public static VisaRecordRequestDto fromEntity(VisaRecord entity) {
        VisaRecordRequestDto dto = new VisaRecordRequestDto();
        dto.setCompanyName(entity.getCompanyName());
        dto.setCompanyAddress(entity.getCompanyAddress());
        dto.setCompanyContact(entity.getCompanyContact());
        dto.setHeadcount(entity.getHeadcount());
        dto.setNationality(entity.getNationality());
        dto.setJobCode(entity.getJobCode());
        dto.setWorkerName(entity.getWorkerName());
        dto.setImmigrationOffice(entity.getImmigrationOffice());
        dto.setApplicationDate(entity.getApplicationDate());
        dto.setApprovalStatus(entity.getApprovalStatus());
        dto.setRemarks(entity.getRemarks());
        return dto;
    }

    // DTO → 신규 Entity (등록용)
    public VisaRecord toEntity() {
        VisaRecord entity = new VisaRecord();
        applyToEntity(entity); // 동일 로직 재사용
        return entity;
    }
    
 // DTO → 기존 Entity에 값 반영 (수정용)
    public void applyToEntity(VisaRecord entity) {
        entity.setCompanyName(this.companyName);
        entity.setCompanyAddress(this.companyAddress);
        entity.setCompanyContact(this.companyContact);
        entity.setHeadcount(this.headcount);
        entity.setNationality(this.nationality);
        entity.setJobCode(this.jobCode);
        entity.setWorkerName(this.workerName);
        entity.setImmigrationOffice(this.immigrationOffice);
        entity.setApplicationDate(this.applicationDate);
        entity.setApprovalStatus(this.approvalStatus);
        entity.setRemarks(this.remarks);
    }

}
