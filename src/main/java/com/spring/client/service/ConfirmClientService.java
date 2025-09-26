// com.spring.client.service.ConfirmClientService
package com.spring.client.service;

import com.spring.client.dto.ConfirmRowDto;
import com.spring.client.enums.ApprStatus;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConfirmClientService {

    Page<ConfirmRowDto> searchRows(ApprStatus status, String q, Pageable pageable);
    
 // ConfirmClientService.java
    Page<ConfirmRowDto> searchFiltered(
    	    ApprStatus status,
    	    String field,         // all | cmpName | bizNo | contactName | contactPhone | proxyExecutor
    	    String keyword,       // 텍스트 검색어
    	    String prxJoinVal,    // "" | "true"
    	    String dateType,      // "created" | "processed"
    	    LocalDate dateFrom,   // 시작일 (nullable)
    	    LocalDate dateTo,     // 종료일 (nullable, inclusive)
    	    Pageable pageable
    );
    
    void applyDecision(Long cmpId,
                       ApprStatus status,
                       String rejectReason,
                       String adminName,
                       boolean sendEmail);

    void applyBatch(List<DecisionCommand> commands, String adminName);
    
    // 🔹 HTML에서 반려탭에서 값만 고치는 인라인 편집(상태 변경 아님)
    void inlineEdit(Long cmpId,
                    String cmpName,
                    String contactName,
                    String contactPhone,
                    String rejectReason,  // REJECTED 탭에서만 의미 있음
                    String adminName);

    // 배치 전용 커맨드 DTO
    class DecisionCommand {
        private Long cmpId;
        private ApprStatus status;
        private String rejectReason;
        private boolean sendEmail;

        public DecisionCommand() {}
        public DecisionCommand(Long cmpId, ApprStatus status, String rejectReason, boolean sendEmail) {
            this.cmpId = cmpId; this.status = status; this.rejectReason = rejectReason; this.sendEmail = sendEmail;
        }
        public Long getCmpId() { return cmpId; }
        public ApprStatus getStatus() { return status; }
        public String getRejectReason() { return rejectReason; }
        public boolean isSendEmail() { return sendEmail; }

        public void setCmpId(Long cmpId) { this.cmpId = cmpId; }
        public void setStatus(ApprStatus status) { this.status = status; }
        public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
        public void setSendEmail(boolean sendEmail) { this.sendEmail = sendEmail; }
    }
    
    
}
