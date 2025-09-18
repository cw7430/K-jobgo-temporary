package com.spring.client.dto.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinRequestDTO {

    // ===== 회사 기본 정보 =====
    @NotBlank(message = "회사명을 입력해 주세요.")
    private String cmpName;

    @NotBlank(message = "대표자 성명을 입력해 주세요.")
    private String ceoName;

    /** bizNo1-3을 합친 값 */
    @NotBlank(message = "사업자등록번호 인증 후 값이 없으면 입력해 주세요.")
    private String bizNo;

    @NotBlank(message = "이메일을 입력해 주세요.")
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    private String bizEmail;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{10,}$",
    message="비밀번호는 영문자와 숫자를 각각 1자 이상 포함해야 합니다.")
    private String bizPwd;

    @NotBlank
    private String confirmPassword;
    
    @AssertTrue(message="비밀번호와 비밀번호 확인이 일치해야 합니다.")
    public boolean isPasswordMatching() {
        return bizPwd != null && bizPwd.equals(confirmPassword);
    }
    
    @NotBlank(message = "우편번호를 입력해 주세요.")
    private String zipCode;

    @NotBlank(message = "기본 주소를 입력해 주세요.")
    private String cmpAddr;

    @NotBlank(message = "상세 주소를 입력해 주세요.")
    private String addrDt;

    /** cmpPhone1-3을 합친 값 */
    @NotBlank(message = "회사 연락처를 입력해 주세요.")
    private String cmpPhone;

    // ===== 동의 체크 =====
    /** 대리 가입 동의 여부 */
    private Boolean prxJoin;
    
    
    /** 대리 가입 시 필수 */
    private String proxyExecutor;   // ✅ 추가

    @AssertTrue(message = "대리 가입을 선택하셨다면 처리 직원명을 입력해 주세요.")
    public boolean isProxyExecutorValid() {
        return prxJoin == null || !prxJoin || (proxyExecutor != null && !proxyExecutor.isBlank());
    }

    /** 첨부파일 확인 동의 여부 */
    private Boolean fileConfirm;
    @AssertTrue(message = "대리 가입일 경우 첨부파일 확인 동의가 필요합니다.")
    public boolean isFileConfirmValid() {
        // prxJoin == null → 아직 선택 안 했을 때도 true 처리해서 다른 검증에 맡김
        // prxJoin == false → 직접가입 → fileConfirm 체크 불필요
        // prxJoin == true  → 대리가입 → 반드시 fileConfirm = true
        return prxJoin == null || !prxJoin || fileConfirm;
    }

    /** 이용약관 동의 여부 */
    @AssertTrue(message = "이용약관 및 개인정보 처리방침에 동의하셔야 합니다.")
    private Boolean agrTerms;

    // ===== 파일 첨부 =====
    @NotNull(message = "사업자등록증 사본을 첨부해 주세요.")
    private MultipartFile bizFileLicense;

    @NotNull(message = "담당자 명함을 첨부해 주세요.")
    private MultipartFile bizFileCard;


    // ===== 담당자 정보 =====
    @NotBlank(message = "담당자 성명을 입력해 주세요.")
    private String empName;

    @NotBlank(message = "담당자 직급/직함을 입력해 주세요.")
    private String empTitle;

    /** empPhone1-3을 합친 값 */
    @NotBlank(message = "담당자 연락처를 입력해 주세요.")
    private String empPhone;


    // ===== 구인조건 (선택 입력) =====
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

}