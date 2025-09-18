package com.spring.client.repository;

import com.spring.client.entity.CmpJobCondition;
import com.spring.client.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CmpJobConditionRepository 
					extends JpaRepository<CmpJobCondition, Long> { // 구인조건
	
    List<CmpJobCondition> findByCmpInfo_CmpId(Long cmpId);

    List<CmpJobCondition> findByCmpInfo_CmpIdAndStatus(Long cmpId, JobStatus status); // ← 추가

    Page<CmpJobCondition> findByStatus(JobStatus status, Pageable pageable);

    long countByCmpInfo_CmpIdAndStatus(Long cmpId, JobStatus status);
    
    Optional<CmpJobCondition> findTopByCmpInfo_CmpIdOrderByCreatedAtDesc(Long cmpId);
    
    List<CmpJobCondition> findByStatusOrderByCreatedAtDesc(JobStatus status);
    
    List<CmpJobCondition> findAllByOrderByCreatedAtDesc();

}
