package com.spring.client.dto.request;

import com.spring.client.entity.CmpInfo;
import com.spring.client.entity.CmpJobCondition;
import com.spring.client.enums.JobStatus;
import lombok.*;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyEmpForm {

    /** 수정 시 대상 id (신규면 null) */
    private Long jobId;

    /** 낙관적 락 버전 (엔티티에 @Version 있을 때 사용) */
    private Long version;

    // === 구직 조건 폼 필드들 ===
    @Size(max = 255)
    private String jobType;

    @Size(max = 100)
    private String desiredNationality;

    @Min(0)
    private Integer desiredCount;

    @Size(max = 255)
    private String jobCategory;

    @Size(max = 100)
    private String experience;

    @Size(max = 100)
    private String education;

    @Size(max = 255)
    private String qualification;

    private String workingHours; // TEXT

    @Size(max = 100)
    private String breakTime;

    @Size(max = 100)
    private String employmentType;

    @Size(max = 255)
    private String insurance;

    @Size(max = 255)
    private String retirementPay;

    @Min(0)
    private Integer totalCount;

    @Min(0)
    private Integer currentForeigners;

    @Size(max = 255)
    private String dormitory;

    @Size(max = 255)
    private String meal;

    private String jobDescription; // TEXT

    @Size(max = 255)
    private String major;

    @Size(max = 255)
    private String computerSkills;

    @Size(max = 255)
    private String languageSkills;

    @Size(max = 255)
    private String preferredConditions;

    @Size(max = 255)
    private String otherPreferredConditions;

    private String otherNotes; // TEXT

    /** 화면 표시용(읽기 전용 권장) – 서버에서만 변경 */
    private JobStatus status;

    /* -------------------- 편의 메서드 -------------------- */

    /** 수정 화면 진입 시: 엔티티 → 폼 복사 */
    public static ApplyEmpForm fromEntity(CmpJobCondition e) {
        if (e == null) return new ApplyEmpForm();
        return ApplyEmpForm.builder()
                .jobId(e.getJobId())
                .version(getVersionSafely(e))  // 엔티티에 @Version 있으면 값 채움
                .jobType(e.getJobType())
                .desiredNationality(e.getDesiredNationality())
                .desiredCount(e.getDesiredCount())
                .jobCategory(e.getJobCategory())
                .experience(e.getExperience())
                .education(e.getEducation())
                .qualification(e.getQualification())
                .workingHours(e.getWorkingHours())
                .breakTime(e.getBreakTime())
                .employmentType(e.getEmploymentType())
                .insurance(e.getInsurance())
                .retirementPay(e.getRetirementPay())
                .totalCount(e.getTotalCount())
                .currentForeigners(e.getCurrentForeigners())
                .dormitory(e.getDormitory())
                .meal(e.getMeal())
                .jobDescription(e.getJobDescription())
                .major(e.getMajor())
                .computerSkills(e.getComputerSkills())
                .languageSkills(e.getLanguageSkills())
                .preferredConditions(e.getPreferredConditions())
                .otherPreferredConditions(e.getOtherPreferredConditions())
                .otherNotes(e.getOtherNotes())
                .status(e.getStatus())
                .build();
    }

    /** 폼 → 새 엔티티(기본 ACTIVE; 상태는 서버에서 제어 권장) */
    public CmpJobCondition toNewEntity(CmpInfo info) {
        CmpJobCondition e = new CmpJobCondition();
        e.setCmpInfo(info);
        e.setStatus(JobStatus.ACTIVE);        // 기본값
        copyTo(e);                            // 나머지 필드 복사
        return e;
    }

    /** 폼 → 기존 엔티티에 값 복사(회원 입력만 반영; status는 건드리지 않음) */
    public void copyTo(CmpJobCondition e) {
        e.setJobType(trim(jobType));
        e.setDesiredNationality(trim(desiredNationality));
        e.setDesiredCount(desiredCount);
        e.setJobCategory(trim(jobCategory));
        e.setExperience(trim(experience));
        e.setEducation(trim(education));
        e.setQualification(trim(qualification));
        e.setWorkingHours(trim(workingHours));
        e.setBreakTime(trim(breakTime));
        e.setEmploymentType(trim(employmentType));
        e.setInsurance(trim(insurance));
        e.setRetirementPay(trim(retirementPay));
        e.setTotalCount(totalCount);
        e.setCurrentForeigners(currentForeigners);
        e.setDormitory(trim(dormitory));
        e.setMeal(trim(meal));
        e.setJobDescription(trim(jobDescription));
        e.setMajor(trim(major));
        e.setComputerSkills(trim(computerSkills));
        e.setLanguageSkills(trim(languageSkills));
        e.setPreferredConditions(trim(preferredConditions));
        e.setOtherPreferredConditions(trim(otherPreferredConditions));
        e.setOtherNotes(trim(otherNotes));

        // ⚠ status는 관리자/서버 로직에서만 변경하도록 여기선 복사 X
        // if (status != null) e.setStatus(status);  // <<-- 사용 금지(관리자 전용)
    }

    private static String trim(String s) {
        return (s == null ? null : s.trim());
    }

    // 엔티티에 @Version 없으면 null
    private static Long getVersionSafely(CmpJobCondition e) {
        try {
            var f = CmpJobCondition.class.getDeclaredField("version");
            f.setAccessible(true);
            Object v = f.get(e);
            return (v instanceof Number) ? ((Number) v).longValue() : null;
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            return null;
        }
    }
}
