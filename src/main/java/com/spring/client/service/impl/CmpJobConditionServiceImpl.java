// src/main/java/com/spring/client/service/impl/CmpJobConditionServiceImpl.java
package com.spring.client.service.impl;

import com.spring.client.dto.CmpJobConditionCancelLogDto;
import com.spring.client.dto.CmpJobConditionDto;
import com.spring.client.dto.request.ApplyEmpForm;
import com.spring.client.entity.CmpInfo;
import com.spring.client.entity.CmpJobCondition;
import com.spring.client.enums.JobStatus;
import com.spring.client.repository.CmpInfoRepository;
import com.spring.client.repository.CmpJobConditionRepository;
import com.spring.client.service.CmpJobConditionDeleteService;
import com.spring.client.service.CmpJobConditionService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CmpJobConditionServiceImpl implements CmpJobConditionService {

    private final CmpJobConditionRepository repo;
    private final CmpInfoRepository cmpInfoRepo;
    private final CmpJobConditionDeleteService deleteLogService;

    /* =======================================================
     * CRUD / 상태 변경
     * ======================================================= */

    @Override
    @Transactional
    public CmpJobConditionDto create(CmpJobConditionDto dto) {
        CmpInfo cmp = cmpInfoRepo.findById(dto.getCmpId())
                .orElseThrow(() -> new IllegalArgumentException("회사 없음: " + dto.getCmpId()));
        CmpJobCondition saved = repo.save(dto.toNewEntity(cmp));
        return CmpJobConditionDto.from(saved);
    }

    @Override
    @Transactional
    public CmpJobConditionDto update(Long jobId, CmpJobConditionDto dto) {
        CmpJobCondition e = repo.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("구인조건 없음: " + jobId));

        // 완료/취소 상태는 수정 불가
        if (e.getStatus() == JobStatus.COMPLETED || e.getStatus() == JobStatus.CANCELLED) {
            throw new IllegalStateException("완료/취소된 구인조건은 수정할 수 없습니다.");
        }

        // 부분 업데이트 (null 무시)
        dto.copyTo(e, true);
        CmpJobCondition saved = repo.save(e);
        return CmpJobConditionDto.from(saved);
    }

    @Override
    @Transactional
    public void cancel(Long jobId, String reason, String cancelledBy) {
        CmpJobCondition e = repo.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("구인조건 없음: " + jobId));

        if (e.getStatus() == JobStatus.CANCELLED) return; // 멱등
        if (e.getStatus() == JobStatus.COMPLETED) {
            throw new IllegalStateException("완료된 구인조건은 취소할 수 없습니다.");
        }

        e.setStatus(JobStatus.CANCELLED);
        e.setCancelledAt(LocalDateTime.now());
        repo.save(e);

        // 선택: 취소 로그 남기기
        if (reason != null || cancelledBy != null) {
            deleteLogService.logCancel(
                CmpJobConditionCancelLogDto.builder()
                    .jobId(e.getJobId())
                    .cancelReason(reason)
                    .cancelledBy(cancelledBy)
                    .build()
            );
        }
    }

    @Override
    @Transactional
    public void complete(Long jobId, String completedBy) {
        CmpJobCondition e = repo.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("구인조건 없음: " + jobId));

        if (e.getStatus() == JobStatus.CANCELLED) {
            throw new IllegalStateException("취소된 구인조건은 완료할 수 없습니다.");
        }
        if (e.getStatus() == JobStatus.COMPLETED) return; // 멱등

        e.setStatus(JobStatus.COMPLETED);
        e.setCompletedAt(LocalDateTime.now());
        // 처리 메타(담당자/일시)
        e.setHandledBy(completedBy);
        e.setHandledAt(LocalDateTime.now());

        repo.save(e);
    }

    /* =======================================================
     * 조회
     * ======================================================= */

    @Override
    @Transactional(readOnly = true)
    public CmpJobConditionDto findById(Long jobId) {
        CmpJobCondition e = repo.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("구인조건 없음: " + jobId));
        return CmpJobConditionDto.from(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CmpJobConditionDto> findByCmpIdAndStatus(Long cmpId, JobStatus status) {
        return repo.findByCmpInfo_CmpIdAndStatus(cmpId, status)
                .stream()
                .map(CmpJobConditionDto::from)
                .collect(Collectors.toList());
    }

    /* =======================================================
     * 폼 로딩 / 회원 저장
     * ======================================================= */

    @Override
    @Transactional(readOnly = true)
    public CmpJobCondition loadDraftOrNew(Long cmpId) {
        return repo.findTopByCmpInfo_CmpIdOrderByCreatedAtDesc(cmpId)
                .orElseGet(() -> {
                    CmpJobCondition j = new CmpJobCondition();
                    j.setStatus(JobStatus.ACTIVE);
                    return j;
                });
    }

    @Override
    @Transactional
    public void saveOrSubmit(Long cmpId, ApplyEmpForm f) {
        CmpInfo info = cmpInfoRepo.findById(cmpId)
                .orElseThrow(() -> new EntityNotFoundException("회사 없음: " + cmpId));

        CmpJobCondition jc;

        if (f.getJobId() != null) {
            // 특정 요청 편집
            jc = repo.findById(f.getJobId())
                    .orElseThrow(() -> new EntityNotFoundException("구인조건 없음: jobId=" + f.getJobId()));

            // 소유권 검증 (타 회사 데이터 수정 방지)
            if (!jc.getCmpInfo().getCmpId().equals(cmpId)) {
                throw new IllegalStateException("해당 요청은 이 회사 소유가 아닙니다.");
            }

            // 상태 재검증(동시성 대응)
            if (jc.getStatus() != JobStatus.ACTIVE) {
                throw new IllegalStateException("관리자 처리 등으로 더 이상 수정할 수 없습니다.");
            }

        } else {
            // 새 요청 or 최신 초안 이어서 수정
            jc = repo.findTopByCmpInfo_CmpIdOrderByCreatedAtDesc(cmpId)
                    .orElseGet(() -> {
                        CmpJobCondition n = new CmpJobCondition();
                        n.setCmpInfo(info);
                        n.setStatus(JobStatus.ACTIVE);
                        return n;
                    });

            // 최신건이 ACTIVE가 아니면 "진짜 새 요청"을 생성
            if (jc.getJobId() != null && jc.getStatus() != JobStatus.ACTIVE) {
                jc = new CmpJobCondition();
                jc.setCmpInfo(info);
                jc.setStatus(JobStatus.ACTIVE);
            }
        }

        // 회원 입력 필드만 엔티티에 반영 (status는 여기서 변경 X)
        f.copyTo(jc);

        // 안전 기본값
        if (jc.getStatus() == null) jc.setStatus(JobStatus.ACTIVE);

        repo.save(jc);
    }

    /* =======================================================
     * 관리자 목록/상세/처리
     * ======================================================= */

    @Override
    @Transactional(readOnly = true)
    public List<CmpJobCondition> findAll(String status) {
        if (status == null || status.isBlank()) {
            return repo.findAllByOrderByCreatedAtDesc();
        }
        JobStatus st = JobStatus.valueOf(status);
        return repo.findByStatusOrderByCreatedAtDesc(st);
    }

    @Override
    @Transactional(readOnly = true)
    public CmpJobCondition loadLatestByCompany(Long cmpId) {
        return repo.findTopByCmpInfo_CmpIdOrderByCreatedAtDesc(cmpId)
                .orElseThrow(() -> new EntityNotFoundException("요청 없음: cmpId=" + cmpId));
    }

    @Override
    @Transactional
    public void adminHandle(Long jobId, String handledBy, String note, JobStatus newStatus) {
        CmpJobCondition jc = repo.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("요청 없음: jobId=" + jobId));

        // 처리자/비고/일시 기록
        jc.setHandledBy(handledBy);
        jc.setHandledAt(LocalDateTime.now());
        jc.setAdminNote(note);

        // 상태 변경(선택)
        if (newStatus != null) {
            jc.setStatus(newStatus);
            if (newStatus == JobStatus.COMPLETED) {
                jc.setCompletedAt(LocalDateTime.now());
            } else if (newStatus == JobStatus.CANCELLED) {
                jc.setCancelledAt(LocalDateTime.now());
            }
        }

        repo.save(jc);
    }

	@Override
	public Page<CmpJobCondition> searchForAdmin(Object object, JobStatus status, LocalDate from, LocalDate to,
			boolean includeDeleted, Long mineAdminId, Pageable pageable) {
		// TODO Auto-generated method stub
		return null;
	}
}
