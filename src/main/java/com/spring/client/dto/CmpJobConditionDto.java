package com.spring.client.dto;

import com.spring.client.entity.CmpInfo;
import com.spring.client.entity.CmpJobCondition;
import com.spring.client.enums.JobStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmpJobConditionDto {
    private Long jobId;
    private Long cmpId;

    // (선택) 낙관적 락
    private Long version;

    // ✅ 뷰에서 요구하는 필드
    private String cmpName;        // 회사명
    private String contactName;    // 담당자
    private String contactPhone;   // 연락처
    private String ownerAdminName; // 담당 관리자 표시용(있으면)

    // ---- 구인조건 기본 필드 ----
    private String jobType;
    private String desiredNationality;
    private Integer desiredCount;
    private String jobCategory;
    private String experience;
    private String education;
    private String qualification;
    private String workingHours;
    private String breakTime;
    private String employmentType;
    private String insurance;
    private String retirementPay;
    private Integer totalCount;
    private Integer currentForeigners;
    private String dormitory;
    private String meal;
    private String jobDescription;
    private String major;
    private String computerSkills;
    private String languageSkills;
    private String preferredConditions;
    private String otherPreferredConditions;
    private String otherNotes;

    // ---- 상태 & 타임스탬프 ----
    private JobStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;

    // ---- 관리자 처리 메타 ----
    private String handledBy;        // 처리 담당자
    private LocalDateTime handledAt; // 처리 일시
    private String adminNote;        // 처리 비고

    /** Entity → DTO */
    public static CmpJobConditionDto from(CmpJobCondition e) {
        CmpJobConditionDto dto = CmpJobConditionDto.builder()
                .jobId(e.getJobId())
                .cmpId(e.getCmpInfo() != null ? e.getCmpInfo().getCmpId() : null)
                .version(getVersionSafely(e))
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
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .completedAt(e.getCompletedAt())
                .cancelledAt(e.getCancelledAt())
                .handledBy(e.getHandledBy())
                .handledAt(e.getHandledAt())
                .adminNote(e.getAdminNote())
                .build();

        // ✅ 연관 엔티티에서 안전하게 값 꺼내기 (필드명/구조에 맞춰 안전 접근)
        CmpInfo cmp = e.getCmpInfo();
        if (cmp != null) {
            // 회사명
            try { dto.setCmpName(nz(cmp.getCmpName())); } catch (Exception ignore) {}

            // 담당자명/연락처: contacts의 첫 원소 우선, 없으면 회사 대표번호로 폴백
            String cName = null;
            String cPhone = null;
            try {
                var contacts = cmp.getContacts(); // List<...> 가정
                if (contacts != null && !contacts.isEmpty()) {
                    Object c = contacts.get(0); // 타입 의존 제거(리플렉션 사용)
                    try {
                        var m = c.getClass().getMethod("getEmpName");
                        Object v = m.invoke(c);
                        if (v != null) cName = v.toString();
                    } catch (NoSuchMethodException ignore) {}
                    try {
                        var m = c.getClass().getMethod("getEmpPhone");
                        Object v = m.invoke(c);
                        if (v != null) cPhone = v.toString();
                    } catch (NoSuchMethodException ignore) {}
                }
            } catch (Exception ignore) {}

            if (isBlank(cPhone)) {
                try { cPhone = nz(cmp.getCmpPhone()); } catch (Exception ignore) {}
            }

            dto.setContactName(nz(cName));
            dto.setContactPhone(nz(cPhone));
        }

        // (선택) 담당 관리자명: 실제 연관 필드가 있을 때만
        // if (e.getOwnerAdmin() != null) {
        //     dto.setOwnerAdminName(e.getOwnerAdmin().getAdminName());
        // }

        return dto;
    }

    private static String nz(String v) { return v == null ? "" : v; }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    // 엔티티에 @Version 없으면 null 리턴
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

    /** DTO → 새 Entity (insert 용)  */
    public CmpJobCondition toNewEntity(CmpInfo cmpInfo) {
        return CmpJobCondition.builder()
                .cmpInfo(cmpInfo)
                .jobType(jobType)
                .desiredNationality(desiredNationality)
                .desiredCount(desiredCount)
                .jobCategory(jobCategory)
                .experience(experience)
                .education(education)
                .qualification(qualification)
                .workingHours(workingHours)
                .breakTime(breakTime)
                .employmentType(employmentType)
                .insurance(insurance)
                .retirementPay(retirementPay)
                .totalCount(totalCount)
                .currentForeigners(currentForeigners)
                .dormitory(dormitory)
                .meal(meal)
                .jobDescription(jobDescription)
                .major(major)
                .computerSkills(computerSkills)
                .languageSkills(languageSkills)
                .preferredConditions(preferredConditions)
                .otherPreferredConditions(otherPreferredConditions)
                .otherNotes(otherNotes)
                .status(status != null ? status : JobStatus.ACTIVE)
                .build();
    }

    /**
     * DTO 값으로 엔티티 갱신(부분 업데이트)
     * - 클라이언트(회원) 입력을 반영: 관리자 메타는 절대 건드리지 않음
     */
    public void copyTo(CmpJobCondition target, boolean ignoreNulls) {
        if (!ignoreNulls || jobType != null) target.setJobType(jobType);
        if (!ignoreNulls || desiredNationality != null) target.setDesiredNationality(desiredNationality);
        if (!ignoreNulls || desiredCount != null) target.setDesiredCount(desiredCount);
        if (!ignoreNulls || jobCategory != null) target.setJobCategory(jobCategory);
        if (!ignoreNulls || experience != null) target.setExperience(experience);
        if (!ignoreNulls || education != null) target.setEducation(education);
        if (!ignoreNulls || qualification != null) target.setQualification(qualification);
        if (!ignoreNulls || workingHours != null) target.setWorkingHours(workingHours);
        if (!ignoreNulls || breakTime != null) target.setBreakTime(breakTime);
        if (!ignoreNulls || employmentType != null) target.setEmploymentType(employmentType);
        if (!ignoreNulls || insurance != null) target.setInsurance(insurance);
        if (!ignoreNulls || retirementPay != null) target.setRetirementPay(retirementPay);
        if (!ignoreNulls || totalCount != null) target.setTotalCount(totalCount);
        if (!ignoreNulls || currentForeigners != null) target.setCurrentForeigners(currentForeigners);
        if (!ignoreNulls || dormitory != null) target.setDormitory(dormitory);
        if (!ignoreNulls || meal != null) target.setMeal(meal);
        if (!ignoreNulls || jobDescription != null) target.setJobDescription(jobDescription);
        if (!ignoreNulls || major != null) target.setMajor(major);
        if (!ignoreNulls || computerSkills != null) target.setComputerSkills(computerSkills);
        if (!ignoreNulls || languageSkills != null) target.setLanguageSkills(languageSkills);
        if (!ignoreNulls || preferredConditions != null) target.setPreferredConditions(preferredConditions);
        if (!ignoreNulls || otherPreferredConditions != null) target.setOtherPreferredConditions(otherPreferredConditions);
        if (!ignoreNulls || otherNotes != null) target.setOtherNotes(otherNotes);
        if (!ignoreNulls || status != null) target.setStatus(status);
        // handledBy/handledAt/adminNote 는 여기서 복사하지 않음(보안/권한)
    }

    /** 관리자 화면에서만 관리자 메타 반영 */
    public void copyAdminMeta(CmpJobCondition target, boolean ignoreNulls) {
        if (!ignoreNulls || handledBy != null) target.setHandledBy(handledBy);
        if (!ignoreNulls || handledAt != null) target.setHandledAt(handledAt);
        if (!ignoreNulls || adminNote != null) target.setAdminNote(adminNote);
    }

    /** 로그/이벤트용 요약 문자열 */
    public static String summarize(CmpJobCondition e) {
        return String.format("jobType=%s, desiredNationality=%s, desiredCount=%s, status=%s",
                e.getJobType(), e.getDesiredNationality(), e.getDesiredCount(), e.getStatus());
    }
    
    // 템플릿 상태 badge
    public String getStatusLabel() {
        return status != null ? status.getLabelKo() : "상태 미정";
    }

}
