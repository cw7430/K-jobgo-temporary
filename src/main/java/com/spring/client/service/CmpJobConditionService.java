package com.spring.client.service;

import com.spring.client.dto.CmpJobConditionDto;
import com.spring.client.dto.request.ApplyEmpForm;
import com.spring.client.entity.CmpJobCondition;
import com.spring.client.enums.JobStatus;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CmpJobConditionService {
    // CRUD
    CmpJobConditionDto create(CmpJobConditionDto dto);
    CmpJobConditionDto update(Long jobId, CmpJobConditionDto dto);

    // 상태 변경
    void cancel(Long jobId, String reason, String cancelledBy);
    void complete(Long jobId, String completedBy);

    // 조회
    CmpJobConditionDto findById(Long jobId);
    List<CmpJobConditionDto> findByCmpIdAndStatus(Long cmpId, JobStatus status);

    // 회원 폼 로딩/저장
    CmpJobCondition loadDraftOrNew(Long cmpId);
    void saveOrSubmit(Long cmpId, ApplyEmpForm form);

    // 관리자 목록/상세/처리
    List<CmpJobCondition> findAll(String status);
    CmpJobCondition loadLatestByCompany(Long cmpId);
    void adminHandle(Long jobId, String handledBy, String note, JobStatus newStatus);

    // (선택) 과거 이름 호환용 위임
    default void applyComplete(Long jobId, String completedBy) {
        complete(jobId, completedBy);
    }
    // 관리자 검색(페이지)
    Page<CmpJobCondition> searchForAdmin(
        String q,
        JobStatus status,
        LocalDate from,
        LocalDate to,
        boolean includeDeleted,
        Long mineAdminId,           // null이면 미적용
        Pageable pageable
    );
    
    // 고객 검색(페이지) – 본인 회사 건만
    Page<CmpJobCondition> searchForClient(
        Long cmpId,
        String q,
        JobStatus status,
        Pageable pageable
    );
    
    // 고객 상세(소유권 검사 포함)
    CmpJobCondition loadByIdForCompany(Long jobId, Long cmpId);
}
