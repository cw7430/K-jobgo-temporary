package com.spring.auth.service;

public interface PasswordResetService {
    /**
     * 비밀번호 재설정 링크 메일 발송 요청
     * (이메일 존재 여부와 무관하게 동일하게 동작/응답하도록 내부에서 처리)
     */
    void requestReset(String email, String baseUrl);

    /**
     * 토큰 유효성(만료/사용 여부)만 사전 검증 (폼 렌더 전에 호출)
     */
    boolean validateToken(String rawToken);

    /**
     * 토큰으로 실제 비밀번호 변경 (1회용)
     */
    void resetPassword(String rawToken, String newPassword);
}
