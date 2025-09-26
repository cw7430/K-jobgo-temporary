package com.spring.client.repository;

import com.spring.client.dto.ConfirmRowDto;
import com.spring.client.entity.CmpInfo;
import com.spring.client.dto.CmpInfoDto;
import com.spring.client.enums.ApprStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CmpInfoRepository extends JpaRepository<CmpInfo, Long> {

    // ── 공통 유틸/단건/카운트 ─────────────────────────────────────
    boolean existsByBizEmail(String bizEmail);
    boolean existsByBizNo(String bizNo);
    boolean existsByBizNoAndIsDelFalse(String bizNo);

    Optional<CmpInfo> findByBizEmail(String bizEmail);
    Optional<CmpInfo> findByCmpIdAndIsDelFalse(Long cmpId);

    long countByApprStatusAndIsDelFalse(ApprStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update CmpInfo c set c.lastLogin = CURRENT_TIMESTAMP where c.cmpId = :cmpId")
    int touchLastLogin(@Param("cmpId") Long cmpId);

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

    // ── ① 탭 요약용: 상태만으로 전체 리스트 (정렬: 최신 신청일) ─────────
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

    // ── ② 키워드 검색(업체/담당자/연락처/사업자번호) + 상태 + 페이지 ───────
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
      where i.apprStatus = :status
        and ( :q is null or :q = '' or
              i.cmpName     like concat('%', :q, '%') or
              cont.empName  like concat('%', :q, '%') or
              cont.empPhone like concat('%', :q, '%') or
              i.bizNo       like concat('%', :q, '%')
            )
      order by i.crtDt desc
    """)
    Page<ConfirmRowDto> searchRows(@Param("status") ApprStatus status,
                                   @Param("q") String q,
                                   Pageable pageable);

    // ── ③ 개별 컬럼 필터 + 상태 + 페이지 ───────────────────────────
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
    		    and (:cmpName       is null or i.cmpName      like concat('%', :cmpName, '%'))
    		    and (:bizNo         is null or i.bizNo        like concat('%', :bizNo, '%'))
    		    and (:contactName   is null or cont.empName   like concat('%', :contactName, '%'))
    		    and (:contactPhone  is null or cont.empPhone  like concat('%', :contactPhone, '%'))
    		    and (:prxJoin       is null or i.prxJoin = :prxJoin)
    		    and (:createdFrom   is null or i.crtDt        >= :createdFrom)
    		    and (:createdTo     is null or i.crtDt        <  :createdTo)
    		    and (:processedFrom is null or i.processedAt  >= :processedFrom)
    		    and (:processedTo   is null or i.processedAt  <  :processedTo)
    		    and (:proxyExecutor is null or i.proxyExecutor like concat('%', :proxyExecutor, '%'))
    		  order by i.crtDt desc
    		""")
    		Page<ConfirmRowDto> findRowsFiltered(
    		        @Param("status") ApprStatus status,
    		        @Param("cmpName") String cmpName,
    		        @Param("bizNo") String bizNo,
    		        @Param("contactName") String contactName,
    		        @Param("contactPhone") String contactPhone,
    		        @Param("prxJoin") Boolean prxJoin,
    		        @Param("createdFrom") LocalDateTime createdFrom,
    		        @Param("createdTo") LocalDateTime createdTo,
    		        @Param("processedFrom") LocalDateTime processedFrom,
    		        @Param("processedTo") LocalDateTime processedTo,
    		        @Param("proxyExecutor") String proxyExecutor,
    		        Pageable pageable
    		);
}
