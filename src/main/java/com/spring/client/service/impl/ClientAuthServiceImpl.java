// src/main/java/com/spring/client/service/impl/ClientAuthServiceImpl.java
package com.spring.client.service.impl;

import com.spring.client.entity.CmpInfo;
import com.spring.client.repository.CmpInfoRepository;
import com.spring.client.service.ClientAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientAuthServiceImpl implements ClientAuthService {

    private final CmpInfoRepository repo;
    private final PasswordEncoder encoder;

    @Override
    public CmpInfo authenticate(String email, String rawPassword) {
        var opt = repo.findByBizEmail(email);
        if (opt.isEmpty()) return null;
        var cmp = opt.get();
        if (!encoder.matches(rawPassword, cmp.getBizPwd())) return null;
        return cmp;
    }
}
