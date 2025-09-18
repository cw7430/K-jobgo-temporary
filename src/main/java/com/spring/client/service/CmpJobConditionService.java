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
	Page<CmpJobCondition> searchForAdmin(Object object, JobStatus status, LocalDate from, LocalDate to,
			boolean includeDeleted, Long mineAdminId, Pageable pageable);
}
