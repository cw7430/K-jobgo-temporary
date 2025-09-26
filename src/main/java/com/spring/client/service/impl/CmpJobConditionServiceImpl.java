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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
        CmpJobCondition e = repo.findWithCmpAndContacts(jobId)
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
    @Transactional(readOnly = true)
    public Page<CmpJobCondition> searchForAdmin(
            String q,
            List<JobStatus> statuses,
            LocalDate from,
            LocalDate to,
            boolean includeDeleted, // 현재 스키마엔 삭제필드 없음 → 미사용
            Long mineAdminId,       // 현재 스키마엔 owner FK 없음 → 미사용
            Pageable pageable
    ) {
        if (pageable == null) {
            pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "jobId"));
        }
        if (q == null) q = "";

        Specification<CmpJobCondition> spec = Specification.where(null);
     // ✅ 상태 IN 필터
        if (statuses != null && !statuses.isEmpty()) {
            // IN_PROGRESS 버킷(= 네 가지 상태만 정확히 온 경우)일 때 NULL도 포함
            boolean treatNullAsInProgress =
                statuses.size() == 4 &&
                statuses.contains(JobStatus.ACTIVE) &&
                statuses.contains(JobStatus.PENDING) &&
                statuses.contains(JobStatus.IN_PROGRESS) &&
                statuses.contains(JobStatus.ON_HOLD);
            
            spec = spec.and((root, query, cb) -> {
                var path = root.get("status");
                var in = cb.in(path);
                statuses.forEach(in::value);
                return treatNullAsInProgress ? cb.or(in, cb.isNull(path)) : in;
            });
        }

        // 날짜 필터
        if (from != null) {
            spec = spec.and((root, cq, cb) ->
                cb.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay()));
        }
        if (to != null) {
            spec = spec.and((root, cq, cb) ->
                cb.lessThan(root.get("createdAt"), to.plusDays(1).atStartOfDay()));
        }

        // 키워드 필터
        if (!q.isBlank()) {
            String like = "%" + q + "%";
            spec = spec.and((root, cq, cb) -> cb.or(
                cb.like(root.get("cmpInfo").get("cmpName"), like),
                cb.like(root.get("jobType"), like),
                cb.like(root.get("jobCategory"), like),
                cb.like(root.get("desiredNationality"), like),
                cb.like(root.get("adminNote"), like)
            ));
        }

        return repo.findAll(spec, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CmpJobCondition> searchForClient(Long cmpId, String q, JobStatus status, Pageable pageable) {
        if (pageable == null) {
            pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "jobId"));
        }
        if (q == null) q = "";

        Specification<CmpJobCondition> spec =
            (root, cq, cb) -> cb.equal(root.get("cmpInfo").get("cmpId"), cmpId);

        if (status != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("status"), status));
        }

        if (!q.isBlank()) {
            String like = "%" + q + "%";
            spec = spec.and((root, cq, cb) -> cb.or(
                cb.like(root.get("jobType"), like),
                cb.like(root.get("jobCategory"), like),
                cb.like(root.get("desiredNationality"), like),
                cb.like(root.get("adminNote"), like)
            ));
        }

        Page<CmpJobCondition> page = repo.findAll(spec, pageable);
        return (page != null) ? page : Page.empty(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public CmpJobCondition loadByIdForCompany(Long jobId, Long cmpId) {
        CmpJobCondition e = repo.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("요청 없음: jobId=" + jobId));
        if (!e.getCmpInfo().getCmpId().equals(cmpId)) {
            throw new IllegalStateException("해당 요청은 이 회사 소유가 아닙니다.");
        }
        return e;
    }
    
    @Override
    @Transactional(readOnly = true)
    public CmpJobCondition findEntity(Long jobId) {
        return repo.findById(jobId)
                   .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신청입니다. id=" + jobId));
    }

    @Override
    public void changeStatus(Long jobId, JobStatus to, String actorName) {
        CmpJobCondition job = findEntity(jobId);
        JobStatus from = job.getStatus();

        // 허용 전환만 통과 (enum에 nextAllowed() 구현했다고 가정)
        if (from == null || !from.nextAllowed().contains(to)) {
            throw new IllegalStateException("허용되지 않은 상태 전환입니다. (" +
                    (from == null ? "null" : from.getLabelKo()) + " -> " + to.getLabelKo() + ")");
        }

        job.setStatus(to);
        job.setHandledBy(actorName);
        job.setHandledAt(java.time.LocalDateTime.now());
        // 낙관적 락(@Version)이 있다면 JPA가 버전 체크

        repo.save(job);
    }

    @Override
    public CmpJobCondition save(CmpJobCondition job) {
        return repo.save(job);
    }
}
