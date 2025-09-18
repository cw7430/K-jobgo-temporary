// src/main/java/com/spring/config/CompanyUserDetailsService.java
package com.spring.config;

import com.spring.client.entity.CmpInfo;
import com.spring.client.enums.ApprStatus;
import com.spring.client.repository.CmpInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 기업(클라이언트) 계정용 UserDetailsService.
 * 폼 로그인(/login) 시, 이메일로 CmpInfo를 조회하여 ROLE_COMPANY를 부여합니다.
 */
@Service
@RequiredArgsConstructor
public class CompanyUserDetailsService implements UserDetailsService {

    private final CmpInfoRepository cmpInfoRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1) 이메일로 기업 사용자 조회 (삭제되지 않은 계정만)
        CmpInfo c = cmpInfoRepository.findByBizEmail(email)
                .filter(x -> Boolean.FALSE.equals(x.isDel()))
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 계정입니다: " + email));

        // 2) 승인된 계정만 로그인 허용 (필요 없으면 이 블록 제거)
        ApprStatus status = c.getApprStatus();
        if (status == null || status != ApprStatus.APPROVED) {
            throw new DisabledException("승인되지 않은 계정입니다.");
        }

        // 3) 스프링 시큐리티 사용자로 변환 (DB에는 BCrypt 해시가 저장되어 있어야 함)
        return User.withUsername(c.getBizEmail())
                   .password(c.getBizPwd())   // 반드시 BCrypt 등으로 인코딩되어 있어야 합니다.
                   .roles("COMPANY")          // → ROLE_COMPANY
                   .accountLocked(false)
                   .disabled(false)
                   .build();
    }
}
