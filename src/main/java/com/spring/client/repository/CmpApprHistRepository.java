package com.spring.client.repository;

import com.spring.client.entity.CmpApprHist;
import com.spring.client.enums.EmailStatus;
import org.springframework.data.jpa.repository.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CmpApprHistRepository extends JpaRepository<CmpApprHist, Long>{ // 승인 상태

    List<CmpApprHist> findByCmpInfo_CmpIdOrderByCrtDtDesc(Long cmpId);

    // 이메일 발송 상태 업데이트 (선택)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update CmpApprHist h set h.emailStatus = :status, h.emailSentAt = :sentAt, h.emailErrorMsg = :errorMsg where h.apprId = :apprId")
    int updateEmailStatus(Long apprId, EmailStatus status, LocalDateTime sentAt, String errorMsg);
    
    Optional<CmpApprHist> findTopByCmpInfo_CmpIdAndIsApprFalseOrderByApprDtDesc(Long cmpId);
}
