// com.spring.client.service.impl.ConfirmClientServiceImpl
package com.spring.client.service.impl;

import com.spring.client.entity.CmpApprHist;
import com.spring.client.entity.CmpInfo;
import com.spring.client.enums.ApprStatus;
import com.spring.client.enums.EmailStatus;
import com.spring.client.repository.CmpApprHistRepository;
import com.spring.client.repository.CmpContRepository;
import com.spring.client.repository.CmpInfoRepository;
import com.spring.client.service.ConfirmClientService;
import com.spring.client.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ConfirmClientServiceImpl implements ConfirmClientService {

    private final CmpInfoRepository cmpInfoRepository;
    private final CmpApprHistRepository apprHistRepository;
    private final CmpContRepository cmpContRepository;
    private final EmailService emailService;

    @Override
    public void applyDecision(Long cmpId,
                              ApprStatus status,
                              String rejectReason,
                              String adminName,
                              boolean sendEmail) {

        CmpInfo info = cmpInfoRepository.findById(cmpId)
                .orElseThrow(() -> new IllegalArgumentException("기업을 찾을 수 없습니다. cmpId=" + cmpId));

        if (info.getApprStatus() != ApprStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다. 상태: " + info.getApprStatus());
        }

        String trimmedReason = rejectReason == null ? null : rejectReason.trim();
        if (status == ApprStatus.REJECTED && (trimmedReason == null || trimmedReason.isEmpty())) {
            throw new IllegalArgumentException("반려 사유를 입력한 후 반려 버튼을 다시 클릭해주세요.");
        }
        if (status == ApprStatus.APPROVED) {
            trimmedReason = null; // 승인일 때는 반려사유 무시
        }

        // 1) 상태/처리자/처리시각 저장  ★★ 추가 포인트 ★★
        info.setApprStatus(status);
        info.setProcessedBy(adminName);                 // ← 추가
        info.setProcessedAt(LocalDateTime.now());       // ← 추가
        cmpInfoRepository.save(info);

        // 2) 승인/반려 이력 기록
        CmpApprHist hist = CmpApprHist.builder()
                .cmpInfo(info)
                .isAppr(status == ApprStatus.APPROVED)
                .apprDt(LocalDateTime.now())
                .apprBy(adminName)
                .apprCmt(trimmedReason)
                .emailStatus(EmailStatus.PENDING)
                .build();
        apprHistRepository.save(hist);

        // 3) (옵션) 이메일 발송
        if (sendEmail) {
            try {
                // 구현한 메일 서비스 시그니처에 맞춰 호출하세요.
                // 예) emailService.sendApprovalMail(info, status, trimmedReason);
                if (status == ApprStatus.APPROVED) {
                    emailService.sendApprovalNotification(
                            info.getBizEmail(), info.getCmpName(), info.getBizEmail()
                    );
                } else {
                    emailService.sendRejectionNotification(
                            info.getBizEmail(), info.getCmpName(), List.of(trimmedReason)
                    );
                }
                hist.setEmailStatus(EmailStatus.SENT);
                hist.setEmailSentAt(LocalDateTime.now());
            } catch (Exception ex) {
                hist.setEmailStatus(EmailStatus.FAILED);
                hist.setEmailErrorMsg(ex.getMessage());
            }
            apprHistRepository.save(hist);
        }
    }

    @Override
    public void applyBatch(List<DecisionCommand> commands, String adminName) {
        for (DecisionCommand c : commands) {
            applyDecision(c.getCmpId(), c.getStatus(), c.getRejectReason(), adminName, c.isSendEmail());
        }
    }
    
    @Override
    public void inlineEdit(Long cmpId,
                           String cmpName,
                           String contactName,
                           String contactPhone,
                           String rejectReason,
                           String adminName) {

        CmpInfo info = cmpInfoRepository.findById(cmpId)
            .orElseThrow(() -> new IllegalArgumentException("기업을 찾을 수 없습니다. cmpId=" + cmpId));

        // 1) 회사명
        if (cmpName != null && !cmpName.isBlank()) {
            info.setCmpName(cmpName.trim());
        }

        // 2) 담당자(최신 1건) 수정 — 없으면 정책에 따라 생성하거나 생략
        cmpContRepository.findTopByCmpInfo_CmpIdOrderByEmpIdDesc(cmpId).ifPresent(cont -> {
            if (contactName != null && !contactName.isBlank())  cont.setEmpName(contactName.trim());
            if (contactPhone != null && !contactPhone.isBlank()) cont.setEmpPhone(contactPhone.trim());
        });

        // 3) 반려 상태에서만 반려사유 최신 이력 업데이트
        if (info.getApprStatus() == ApprStatus.REJECTED && rejectReason != null) {
            String trimmed = rejectReason.trim();
            apprHistRepository
                .findTopByCmpInfo_CmpIdAndIsApprFalseOrderByApprDtDesc(cmpId)
                .ifPresent(h -> h.setApprCmt(trimmed.isEmpty() ? null : trimmed));
        }
        // 트랜잭션 종료 시점에 flush 되므로 별도 save() 필요 없음(영속 상태)
    }
}
