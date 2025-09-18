package com.spring.client.service;


import java.util.List;

import com.spring.client.dto.request.JoinRequestDTO;
import com.spring.client.entity.CmpInfo;
import com.spring.client.enums.ApprStatus;

import jakarta.mail.MessagingException;


public interface EmailService {
    /**
     * 가입 신청 확인 이메일을 발송합니다.
     *
     * @param to      수신자 이메일 주소
     * @param subject 메일 제목
     * @param body    메일 본문 (플레인 텍스트)
     */
    // 1) 가입접수 확인 메일, ** (레거시 호환) 가입접수 확인 메일
    void sendJoinConfirmation(String to, String subject, JoinRequestDTO joinRequestDto)
            throws MessagingException;

    // 2) 승인 완료 안내 메일
    /**
     * 관리자 승인 시 HTML 메일 발송
     * @throws javax.mail.MessagingException 전송 실패 시
     */
    void sendApprovalNotification(String to, String companyName, String loginId)
            throws MessagingException;

    // 3) 반려 안내 메일
    /** 신청 반려 안내 메일 (HTML) */
    void sendRejectionNotification(String to, String companyName, List<String> reasons)
            throws MessagingException;
    
    /** (권장) 가입접수 확인 메일 - 신규 진입점 */
    void sendRegistrationNotification(String to, String subject, JoinRequestDTO dto)
            throws MessagingException;

    // 비밀번호 재설정 메일
    void sendPasswordResetLink(String to, String companyName, String resetUrl, int expireMinutes)
            throws jakarta.mail.MessagingException;

	void sendApprovalMail(CmpInfo info, ApprStatus status, String rejectReason);

}
