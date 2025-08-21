package com.spring.dto.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyProfileRequestDto {
    private String agencyName;      // 송출업체명
    private String visaType;        // 비자타입
    private String jobCode;         // 직무코드
    private String employeeNameEn;  // 근로자 이름(영문)
    private String nationalityEn;   // 국적(영문)

    // 다중 파일 첨부
    private List<MultipartFile> files;
}
