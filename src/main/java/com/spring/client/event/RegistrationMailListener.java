package com.spring.client.event;

import com.spring.client.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationMailListener {
    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubmitted(RegistrationSubmittedEvent e) {
        try {
            emailService.sendRegistrationNotification(
                e.email(),
                "[K-jobgo] 회원가입 신청 접수 안내",
                e.payload()
            );
        } catch (MessagingException ex) {
            log.error("가입확인 메일 발송 실패", ex);
        }
    }
}
