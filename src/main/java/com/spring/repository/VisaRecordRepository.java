package com.spring.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.spring.entity.ApprovalStatus;
import com.spring.entity.VisaRecord;

public interface VisaRecordRepository extends JpaRepository<VisaRecord, Long> {

  Page<VisaRecord> findByVisaDeletedFalse(Pageable pageable);

  // 최근 목록(삭제 제외)
  @Query("""
    select v from VisaRecord v
    where v.visaDeleted = false
    order by v.visaId desc
  """)
  List<VisaRecord> findRecentActive(Pageable pageable);

  // 최근 목록(삭제 포함)
  @Query("""
    select v from VisaRecord v
    order by v.visaId desc
  """)
  List<VisaRecord> findRecentIncludingDeleted(Pageable pageable);

  // enum 이름 검색도 허용
  // ✅ 통합 검색: agentId가 null이면 전체, 값이 있으면 해당 에이전트 것만
  @Query(
    value = """
      select v
      from VisaRecord v
      left join v.agent a
      where (:includeDeleted = true or v.visaDeleted = false)
        and (:agentId is null or a.adminId = :agentId)
        and (
             :kw = ''
          or lower(v.companyName)        like lower(concat('%', :kw, '%'))
          or lower(v.companyAddress)     like lower(concat('%', :kw, '%'))
          or lower(v.companyContact)     like lower(concat('%', :kw, '%'))
          or lower(v.nationality)        like lower(concat('%', :kw, '%'))
          or lower(v.jobCode)            like lower(concat('%', :kw, '%'))
          or lower(v.workerName)         like lower(concat('%', :kw, '%'))
          or lower(v.immigrationOffice)  like lower(concat('%', :kw, '%'))
          or lower(cast(v.remarks as string)) like lower(concat('%', :kw, '%'))
          or lower(coalesce(a.adminName, ''))  like lower(concat('%', :kw, '%'))
          or (:kwStatus is not null and v.approvalStatus = :kwStatus)
        )
    """,
    countQuery = """
      select count(v)
      from VisaRecord v
      left join v.agent a
      where (:includeDeleted = true or v.visaDeleted = false)
        and (:agentId is null or a.adminId = :agentId)
        and (
             :kw = ''
          or lower(v.companyName)        like lower(concat('%', :kw, '%'))
          or lower(v.companyAddress)     like lower(concat('%', :kw, '%'))
          or lower(v.companyContact)     like lower(concat('%', :kw, '%'))
          or lower(v.nationality)        like lower(concat('%', :kw, '%'))
          or lower(v.jobCode)            like lower(concat('%', :kw, '%'))
          or lower(v.workerName)         like lower(concat('%', :kw, '%'))
          or lower(v.immigrationOffice)  like lower(concat('%', :kw, '%'))
          or lower(cast(v.remarks as string)) like lower(concat('%', :kw, '%'))
          or lower(coalesce(a.adminName, ''))  like lower(concat('%', :kw, '%'))
          or (:kwStatus is not null and v.approvalStatus = :kwStatus)
        )
    """
  )
  Page<VisaRecord> search(@Param("kw") String kw,
                          @Param("kwStatus") ApprovalStatus kwStatus,
                          @Param("includeDeleted") boolean includeDeleted,
                          @Param("agentId") Long agentId, // ✅ 추가
                          Pageable pageable);
}
