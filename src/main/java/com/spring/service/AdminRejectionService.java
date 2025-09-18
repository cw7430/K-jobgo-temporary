package com.spring.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.spring.client.entity.CmpInfo;
import com.spring.client.repository.CmpInfoRepository;
import com.spring.client.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminRejectionService {
    private final CmpInfoRepository cmpInfoRepository;
    private final EmailService emailService;

    public void rejectCompany(Long cmpInfoId, List<String> reasons, String adminName) {
        CmpInfo cmpInfo = cmpInfoRepository.findById(cmpInfoId)
            .orElseThrow(() -> new EntityNotFoundException("회사정보 없음"));

        cmpInfo.reject(adminName);  // 처리자 기록
        cmpInfoRepository.save(cmpInfo);

        try {
            emailService.sendRejectionNotification(
                cmpInfo.getBizEmail(),
                cmpInfo.getCmpName(),
                reasons
            );
        } catch (MessagingException e) {
            log.error("반려 메일 발송 실패: id={}, to={}",
                      cmpInfoId, cmpInfo.getBizEmail(), e);
        }
    }
}
