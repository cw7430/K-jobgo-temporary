package com.spring.auth.service;

import com.spring.client.service.EmailService;                // 너의 기존 EmailService
import com.spring.client.entity.CmpInfo;                            // 사용자 엔티티 (패키지명에 맞게 조정)
import com.spring.client.repository.CmpInfoRepository;              // 사용자 리포지토리 (패키지명에 맞게 조정)
import com.spring.client.entity.PwdResetToken;                      // 토큰 엔티티 (JPA 매핑 필요)
import com.spring.client.repository.PwdResetTokenRepository;        // 토큰 리포지토리

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final CmpInfoRepository cmpInfoRepository;
    private final PwdResetTokenRepository tokenRepository;
    private final EmailService emailService;                 // 이메일 발송
    private final PasswordEncoder passwordEncoder;           // BCrypt 등

    @Value("${app.reset.token-ttl-minutes:30}")
    private int ttlMinutes;

    @Override
    public void requestReset(String email, String baseUrl) {
        // 이메일이 존재하면 토큰 생성/메일 발송. 존재하지 않아도 동일한 흐름(타이밍/응답)을 유지해 사용자 열거 방지.
        cmpInfoRepository.findByBizEmail(email).ifPresent(user -> {
            // 1) 랜덤 토큰 생성 & 해시 저장
            String rawToken = generateToken();               // URL-safe, 32바이트
            String tokenHash = sha256Hex(rawToken);

            PwdResetToken t = new PwdResetToken();
            t.setCmpId(user.getCmpId());
            t.setTokenHash(tokenHash);
            t.setExpiresAt(LocalDateTime.now().plusMinutes(ttlMinutes));
            tokenRepository.save(t);

            // 2) 링크 생성
            String resetUrl = baseUrl + "/reset-password?token=" + rawToken;

            // 3) 메일 발송 (실패해도 외부 응답은 동일)
            try {
                emailService.sendPasswordResetLink(email, user.getCmpName(), resetUrl, ttlMinutes);
            } catch (Exception ignored) { }
        });
    }

    @Override
    public boolean validateToken(String rawToken) {
        String hash = sha256Hex(rawToken);
        return tokenRepository.findByTokenHash(hash)
                .filter(t -> t.getUsedAt() == null)
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    @Override
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        String hash = sha256Hex(rawToken);

        // 동시성 방지 위해 PESSIMISTIC_WRITE 사용한 리포지토리 메서드 권장
        PwdResetToken token = tokenRepository.findForUpdateByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("invalid token"));

        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("expired or used");
        }

        CmpInfo user = cmpInfoRepository.findById(token.getCmpId())
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        // 비밀번호 변경
        user.setBizPwd(passwordEncoder.encode(newPassword));
        // (선택) 임시비번/강제변경 플래그를 쓰고 있다면 해제
        try {
            var field = CmpInfo.class.getDeclaredField("mustChangePwd");
            field.setAccessible(true);
            field.set(user, Boolean.FALSE);
        } catch (NoSuchFieldException ignored) {
            // 컬럼이 없다면 무시
        } catch (IllegalAccessException ignored) { }

        cmpInfoRepository.save(user);

        // 토큰 사용 처리(1회성)
        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);

        // (선택) 기존 세션 강제 로그아웃/무효화 로직이 있으면 여기서 호출
    }

    // ====== Helpers ======
    private String generateToken() {
        byte[] b = new byte[32];
        new SecureRandom().nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(dig.length * 2);
            for (byte x : dig) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
