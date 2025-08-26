package com.spring.dto.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyProfileRequestDto { // 프로필 등록
    @NotBlank @Size(max = 200)
    private String agencyName;       // 송출업체명

    @NotBlank @Size(max = 50)
    private String visaType;         // 비자타입(영문)

    @NotBlank @Size(max = 50)
    private String jobCode;          // 직무코드(ISCO 4자리 등)

    @NotBlank @Size(max = 150)
    private String employeeNameEn;   // 근로자 이름(영문)

    @NotBlank @Size(max = 100)
    private String nationalityEn;    // 국적(영문)

    // 첨부파일(다중) - 선택값
    @NotEmpty(message = "파일은 반드시 업로드해야 합니다.")
   // private List<MultipartFile> files;
    private @NotNull MultipartFile file; // 파일 1개 첨부

}
