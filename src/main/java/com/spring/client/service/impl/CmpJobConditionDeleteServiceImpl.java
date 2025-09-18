package com.spring.client.service.impl;

import com.spring.client.dto.CmpJobConditionCancelLogDto;
import com.spring.client.entity.CmpJobCondition;
import com.spring.client.entity.CmpJobConditionCancelLog;
import com.spring.client.repository.CmpJobConditionCancelLogRepository;
import com.spring.client.repository.CmpJobConditionRepository;
import com.spring.client.service.CmpJobConditionDeleteService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CmpJobConditionDeleteServiceImpl implements CmpJobConditionDeleteService {

    private final CmpJobConditionCancelLogRepository cancelLogRepo; // cmpjobconditioncanclelog
    private final CmpJobConditionRepository jobCondRepo;
    
    @Override
    @Transactional
    public CmpJobConditionCancelLogDto logCancel(CmpJobConditionCancelLogDto dto) {
        // 1) 대상 구인조건 조회
        CmpJobCondition job = jobCondRepo.findById(dto.getJobId())
                .orElseThrow(() -> new EntityNotFoundException("구인조건이 존재하지 않습니다. jobId=" + dto.getJobId()));

        // 2) 로그 저장
        CmpJobConditionCancelLog log = CmpJobConditionCancelLog.builder()
                .job(job)
                .cancelReason(dto.getCancelReason())
                .cancelledBy(dto.getCancelledBy())
                // cancelledAt은 null이면 @PrePersist에서 now로 세팅됨
                .cancelledAt(dto.getCancelledAt())
                .build();

        log = cancelLogRepo.save(log);

        // 3) 반환 DTO
        return CmpJobConditionCancelLogDto.builder()
                .logId(log.getLogId())
                .jobId(job.getJobId())
                .cancelReason(log.getCancelReason())
                .cancelledBy(log.getCancelledBy())
                .cancelledAt(log.getCancelledAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CmpJobConditionCancelLogDto> findHistoryByJobId(Long jobId) {
        return cancelLogRepo.findByJob_JobIdOrderByCancelledAtDesc(jobId).stream()
                .map(log -> CmpJobConditionCancelLogDto.builder()
                        .logId(log.getLogId())
                        .jobId(log.getJob().getJobId())
                        .cancelReason(log.getCancelReason())
                        .cancelledBy(log.getCancelledBy())
                        .cancelledAt(log.getCancelledAt())
                        .build())
                .collect(Collectors.toList());
    }
}
