package com.spring.client.service;

import com.spring.client.dto.request.JoinRequestDTO;

public interface JoinService {
    /**
     * 회원가입 신청을 받아
     * - cmp_info, cmp_cont, cmp_attach, cmp_job_condition 테이블에 저장
     * - 비밀번호 암호화
     * - 첨부파일 S3 업로드
     * - 승인 대기(PENDING) 상태로 세팅
     * - 가입신청 확인 메일 발송
     */
	
    boolean existsBizNo(String bizNo);
    boolean existsEmail(String email);
    
    Long register(JoinRequestDTO JoinRequestdto);
}
