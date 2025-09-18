package com.spring.client.service.impl;

import com.spring.client.dto.request.JoinRequestDTO;
import com.spring.client.entity.CmpInfo;
import com.spring.client.enums.ApprStatus;
import com.spring.client.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    /** Mailtrap/실운영 무관하게 표시할 발신 정보 */
    @Value("${mail.from:no-reply@kjobgo.com}")
    private String from;

    @Value("${mail.from-name:K-jobgo}")
    private String fromName;

    @Value("${mail.reply-to:}") // 선택적으로 Reply-To 지정 가능
    private String replyTo;

    /**
     * 공통 헤더 세팅 (발신자, Reply-To, 주소 검증)
     */
    private void applyCommonHeaders(MimeMessageHelper helper) throws MessagingException {
        helper.setValidateAddresses(true); // ✅ 주소 형식 자동 검증
        try {
        	helper.setFrom(new InternetAddress(from, fromName, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            log.warn("발신자 이름 인코딩 실패 → 주소만 사용", e);
            helper.setFrom(from);
        }

        if (replyTo != null && !replyTo.isBlank()) {
            try {
            	helper.setReplyTo(new InternetAddress(replyTo, fromName, StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                helper.setReplyTo(replyTo);
            }
        }
    }
    
    /**
     * 제목이 null/공백일 경우 기본값으로 대체
     */
    private String safeSubject(String subject, String defaultSubject) {
        return (subject == null || subject.isBlank()) ? defaultSubject : subject;
    }
    
    /** (레거시 호환) 가입접수 확인 메일 */
    @Override
    public void sendJoinConfirmation(String to, String subject, JoinRequestDTO joinRequestDto)
            throws MessagingException {
        // 내부적으로 신규 메서드 사용 (템플릿 동일)
        sendRegistrationNotification(to, subject, joinRequestDto);
    }

    /** (권장) 가입접수 확인 메일 */
    @Override
    public void sendRegistrationNotification(String to, String subject, JoinRequestDTO joinRequestDto)
            throws MessagingException {

        // 1) 템플릿 변수
        Context ctx = new Context();
        ctx.setVariable("companyName", joinRequestDto.getCmpName()); // 회사명 필드에 맞게 사용

        // 2) 템플릿 렌더링 (templates/client/email-confirm.html)
        String html = templateEngine.process("client/email-confirm", ctx);

        // 3) 메일 구성 & 전송
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        applyCommonHeaders(helper);
        helper.setTo(to);
        helper.setSubject(safeSubject(subject, "[K-jobgo] 가입 신청 접수 안내"));
        helper.setText(html, true);
        
        try {
            mailSender.send(message);
            log.info("가입접수 확인 메일 전송 성공: to={}, subject={}", to, helper.getMimeMessage().getSubject());
        } catch (Exception e) {
            log.error("가입접수 확인 메일 전송 실패: to={}, subject={}", to, helper.getMimeMessage().getSubject(), e);
            throw e;
        }
    }

    /** 승인 완료 안내 메일 */
    @Override
    public void sendApprovalNotification(String to, String companyName, String loginId)
            throws MessagingException {

        // 1) 템플릿 변수
        Context ctx = new Context();
        ctx.setVariable("companyName", companyName);
        ctx.setVariable("loginId", loginId);

        // 2) 템플릿 렌더링 (templates/client/email-approval.html)
        String htmlBody = templateEngine.process("client/email-approval", ctx);

        // 3) 메일 구성 & 전송
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        applyCommonHeaders(helper);
        helper.setTo(to);
        helper.setSubject("[K-jobgo] 회원가입 승인 완료 안내");
        helper.setText(htmlBody, true);

        try {
            mailSender.send(message);
            log.info("승인 완료 메일 전송 성공: to={}, loginId={}", to, loginId);
        } catch (Exception e) {
            log.error("승인 완료 메일 전송 실패: to={}, loginId={}", to, loginId, e);
            throw e;
        }
    }

    /** 반려 안내 메일 */
    @Override
    public void sendRejectionNotification(String to, String companyName, List<String> reasons)
            throws MessagingException {

        // 1) 템플릿 변수
        Context ctx = new Context();
        ctx.setVariable("companyName", companyName);
        ctx.setVariable("reasons", reasons);

        // 2) 템플릿 렌더링 (templates/client/email-rejection.html)
        String htmlBody = templateEngine.process("client/email-rejection", ctx);

        // 3) 메일 구성 & 전송
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        applyCommonHeaders(helper);
        helper.setTo(to);
        helper.setSubject("[K-jobgo] 회원가입 신청 반려 안내");
        helper.setText(htmlBody, true);

        try {
            mailSender.send(message);
            log.info("반려 안내 메일 전송 성공: to={}", to);
        } catch (Exception e) {
            log.error("반려 안내 메일 전송 실패: to={}", to, e);
            throw e;
        }
    }
    
    @Override
    public void sendPasswordResetLink(String to, String companyName, String resetUrl, int expireMinutes)
            throws MessagingException {
    	
        Context ctx = new Context();
        ctx.setVariable("companyName", companyName);
        ctx.setVariable("resetUrl", resetUrl);
        ctx.setVariable("expireMinutes", expireMinutes);

        String html = templateEngine.process("client/email-password-reset", ctx);
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        applyCommonHeaders(helper);
        helper.setTo(to);
        helper.setSubject("[K-jobgo] 비밀번호 재설정 안내");
        helper.setText(html, true);

        try {
            mailSender.send(message);
            log.info("비밀번호 재설정 메일 전송 성공: to={}", to);
        } catch (Exception e) {
            log.error("비밀번호 재설정 메일 전송 실패: to={}", to, e);
            throw e;
        }
    }
    
    @Override
    public void sendApprovalMail(CmpInfo info, ApprStatus status, String rejectReason) {
        if (info == null) {
            throw new IllegalArgumentException("CmpInfo가 null 입니다.");
        }
        final String to = info.getBizEmail();
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("수신자 이메일이 없습니다. cmpId=" + info.getCmpId());
        }
        final String companyName = info.getCmpName();
        try {
            switch (status) {
                case APPROVED -> {
                    // 로그인 아이디는 비즈니스 상 이메일을 아이디로 쓰는 것으로 보임
                    sendApprovalNotification(to, companyName, to);
                }
                case REJECTED -> {
                    // 템플릿은 List<String> 사유를 받으므로 변환
                    List<String> reasons = new ArrayList<>();
                    if (rejectReason != null && !rejectReason.isBlank()) {
                        reasons.add(rejectReason.trim());
                    } else {
                        reasons.add("사유 미기재"); // 최소 1개 보장
                    }
                    sendRejectionNotification(to, companyName, reasons);
                }
                case PENDING -> {
                    // 보통 PENDING 전환 시에는 메일을 보내지 않습니다.
                    // 필요하면 별도 안내 메일을 추가하세요.
                    // 여기서는 아무 것도 하지 않음
                }
            }
        } catch (jakarta.mail.MessagingException e) {
            // 상위 서비스에서 catch 해 EmailStatus=FAILED 처리하므로 런타임으로 래핑
            throw new RuntimeException("메일 전송 실패: " + e.getMessage(), e);
        }
    }
}
