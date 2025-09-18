package com.spring.client.repository;

import com.spring.client.dto.ConfirmRowDto;
import com.spring.client.entity.CmpInfo;
import com.spring.client.enums.ApprStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CmpInfoRepository extends JpaRepository<CmpInfo, Long> {

    // 중복체크
    boolean existsByBizEmail(String bizEmail);
    boolean existsByBizNo(String bizNo);

    boolean existsByBizNoAndIsDelFalse(String bizNo);   // 활성만 중복 체크
    
    // 단건 조회
    Optional<CmpInfo> findByBizEmail(String bizEmail);
    Optional<CmpInfo> findByCmpIdAndIsDelFalse(Long cmpId);

    // 상태별 목록 (페이징)
    Page<CmpInfo> findByApprStatusAndIsDelFalse(ApprStatus apprStatus, Pageable pageable);

    // 마지막 로그인 갱신
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update CmpInfo c set c.lastLogin = CURRENT_TIMESTAMP where c.cmpId = :cmpId")
    int touchLastLogin(@Param("cmpId") Long cmpId);

    // 상태 변경 (직접 JPQL 업데이트 버전 – 서비스에서 엔티티 save() 방식 쓰면 불필요)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update CmpInfo c
         set c.apprStatus = :status,
             c.processedBy = :processedBy,
             c.processedAt = CURRENT_TIMESTAMP
       where c.cmpId = :cmpId
    """)
    int updateApprStatus(@Param("cmpId") Long cmpId,
                         @Param("status") ApprStatus status,
                         @Param("processedBy") String processedBy);

    // 목록 조회용 DTO 투영
    @Query("""
    		  select new com.spring.client.dto.ConfirmRowDto(
    		    i.cmpId, i.cmpName, i.bizNo,
    		    cont.empName, cont.empPhone,
    		    lic.fId, lic.fMime,
    		    card.fId, card.fMime,
    		    i.prxJoin,
    		    i.apprStatus,
    		    hLatest.apprCmt,
    		    i.crtDt,
    		    i.processedAt,
    		    i.proxyExecutor
    		  )
    		  from CmpInfo i
    		  left join CmpCont cont
    		         on cont.cmpInfo = i
    		        and cont.empId = (select max(c2.empId) from CmpCont c2 where c2.cmpInfo = i)
    		  left join CmpAttach lic
    		         on lic.cmpInfo = i
    		        and lic.fileCategory = com.spring.client.enums.FileCategory.BUSINESS_LICENSE
    		  left join CmpAttach card
    		         on card.cmpInfo = i
    		        and card.fileCategory = com.spring.client.enums.FileCategory.BUSINESS_CARD
    		  left join CmpApprHist hLatest
    		         on hLatest.cmpInfo = i
    		        and hLatest.apprDt = (select max(h2.apprDt) from CmpApprHist h2 where h2.cmpInfo = i)
    		  where (:status is null or i.apprStatus = :status)
    		  order by i.crtDt desc
    		""")
    		List<ConfirmRowDto> findRows(@Param("status") ApprStatus status);


    // 탭 카운트용
    long countByApprStatusAndIsDelFalse(ApprStatus status);
}
