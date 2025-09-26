package com.spring.client.repository;

import com.spring.client.entity.CmpJobCondition;
import com.spring.client.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CmpJobConditionRepository 
					extends JpaRepository<CmpJobCondition, Long> ,
	                JpaSpecificationExecutor<CmpJobCondition> { // 구인조건
	
    List<CmpJobCondition> findByCmpInfo_CmpId(Long cmpId);

    List<CmpJobCondition> findByCmpInfo_CmpIdAndStatus(Long cmpId, JobStatus status); // ← 추가

    Page<CmpJobCondition> findByStatus(JobStatus status, Pageable pageable);

    long countByCmpInfo_CmpIdAndStatus(Long cmpId, JobStatus status);
    
    Optional<CmpJobCondition> findTopByCmpInfo_CmpIdOrderByCreatedAtDesc(Long cmpId);
    
    List<CmpJobCondition> findByStatusOrderByCreatedAtDesc(JobStatus status);
    
    List<CmpJobCondition> findAllByOrderByCreatedAtDesc();

    @Query("""
    		select distinct j
    		from CmpJobCondition j
    		join fetch j.cmpInfo ci
    		left join fetch ci.contacts
    		where j.jobId = :jobId
    		""")
    		Optional<CmpJobCondition> findWithCmpAndContacts(@Param("jobId") Long jobId);

}
