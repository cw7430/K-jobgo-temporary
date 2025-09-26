package com.spring.client.service.impl;

import com.spring.client.dto.ApplyEmpAdminLogDTO;
import com.spring.client.dto.ApplyEmpAdminLogViewDTO;
import com.spring.client.entity.*;
import com.spring.client.enums.JobStatus;
import com.spring.client.repository.*;
import com.spring.entity.Admin;
import com.spring.repository.AdminRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ApplyEmpAdminLogService {

    private final ApplyEmpAdminLogRepository logRepo;
    private final CmpJobConditionRepository jobRepo; // 이미 있으리라 가정
    private final AdminRepository adminRepo;         // 이미 있으리라 가정

    @Transactional
    public ApplyEmpAdminLogViewDTO addLog(Long jobId, Long writerAdminId, ApplyEmpAdminLogDTO dto) {
        CmpJobCondition job = jobRepo.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("job not found: " + jobId));

        Admin writer = (writerAdminId != null)
                ? adminRepo.findById(writerAdminId).orElse(null)
                : null;

        ApplyEmpAdminLog log = ApplyEmpAdminLog.builder()
            .job(job)
            .writerAdmin(writer)
            .writerName(writer != null ? writer.getAdminName() : null)
            .statusSnapshot(job.getStatus())
            .handledBy(dto.getHandledBy())
            .counselContent(dto.getCounselContent())
            .referenceNote(dto.getReferenceNote())
            .build();

        logRepo.save(log);

        job.setHandledBy(dto.getHandledBy());
        job.setHandledAt(LocalDateTime.now());
        if (dto.getReferenceNote() != null && !dto.getReferenceNote().isBlank()) {
            job.setAdminNote(dto.getReferenceNote());
        }
        return ApplyEmpAdminLogViewDTO.from(log);
    }

    public Optional<ApplyEmpAdminLogViewDTO> getLatest(Long jobId) {
        return logRepo.findFirstByJob_JobId(
                jobId, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("logId")))
            .map(ApplyEmpAdminLogViewDTO::from);
    }

    public Page<ApplyEmpAdminLogViewDTO> getTimeline(Long jobId, Integer year, Integer month, boolean includeDeleted, Pageable pageable) {
        
    	Page<ApplyEmpAdminLog> page;
        if (year != null && month != null) {
            page = includeDeleted
                ? logRepo.findByJobIdAndYearMonthAll(jobId, year, month, pageable)   // ★ 전체
                : logRepo.findByJobIdAndYearMonthNotDeleted(jobId, year, month, pageable); // 삭제제외
        } else {
            page = includeDeleted
                ? logRepo.findByJob_JobId(jobId, pageable)                           // ★ 전체
                : logRepo.findByJob_JobIdAndDeletedFalse(jobId, pageable);           // 삭제제외
        }
        return page.map(ApplyEmpAdminLogViewDTO::from);
    }
    public List<ApplyEmpAdminLogRepository.YmCount> getYearMonthCounts(Long jobId) {
        return logRepo.countByYearMonthNotDeleted(jobId);
    }

    @Transactional
    public void addStatusChange(Long jobId, Long adminId, JobStatus from, JobStatus to, String referenceNote) {
        if (Objects.equals(from, to)) return;

        CmpJobCondition job = jobRepo.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("job not found: " + jobId));

        Admin admin = (adminId != null) ? adminRepo.findById(adminId).orElse(null) : null;
        String writerName = (admin != null && admin.getAdminName() != null)
                ? admin.getAdminName()
                : "관리자";

        ApplyEmpAdminLog log = ApplyEmpAdminLog.builder()
            .job(job)
            .writerAdmin(admin)
            .writerName(writerName)
            .statusSnapshot(to) // 변경 후
            .handledBy(writerName)
            .counselContent(String.format("[상태변경] %s → %s",
                    from == null ? "(미설정)" : from.getLabelKo(),
                    to   == null ? "(미설정)" : to.getLabelKo()))
            .referenceNote(referenceNote) 
            .build();

        logRepo.save(log);

        job.setHandledBy(writerName);
        job.setHandledAt(LocalDateTime.now());
    }
    
    @Transactional
    public ApplyEmpAdminLogViewDTO softDelete(Long jobId, Long logId, Long adminId, String reason) {
        ApplyEmpAdminLog log = logRepo.findById(logId)
            .orElseThrow(() -> new IllegalArgumentException("log not found: " + logId));

        if (!Objects.equals(log.getJob().getJobId(), jobId))
            throw new IllegalArgumentException("job/log mismatch");

        Admin by = (adminId != null) ? adminRepo.findById(adminId).orElse(null) : null;
        log.softDelete(by, reason);
        return ApplyEmpAdminLogViewDTO.from(log);
    }
}

