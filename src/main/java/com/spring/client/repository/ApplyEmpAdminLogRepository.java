package com.spring.client.repository;

import com.spring.client.entity.ApplyEmpAdminLog;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ApplyEmpAdminLogRepository extends JpaRepository<ApplyEmpAdminLog, Long> {

    /** 최신 1건 (정렬 파라미터로 복합정렬 적용) */
    Optional<ApplyEmpAdminLog> findFirstByJob_JobId(Long jobId, Sort sort);

    // 전체(삭제 포함)
    Page<ApplyEmpAdminLog> findByJob_JobId(Long jobId, Pageable pageable);

    @Query("""
      select l from ApplyEmpAdminLog l
      where l.job.jobId = :jobId
        and function('YEAR', l.createdAt) = :year
        and function('MONTH', l.createdAt) = :month
      order by l.createdAt desc, l.logId desc
    """)
    Page<ApplyEmpAdminLog> findByJobIdAndYearMonthAll(@Param("jobId") Long jobId,
                                                      @Param("year") int year,
                                                      @Param("month") int month,
                                                      Pageable pageable);
    /** 타임라인(삭제 제외) */
    Page<ApplyEmpAdminLog> findByJob_JobIdAndDeletedFalse(Long jobId, Pageable pageable);

    /** 특정 연/월 타임라인(삭제 제외) */
    @Query("""
      select l from ApplyEmpAdminLog l
       where l.job.jobId = :jobId
         and l.deleted = false
         and function('YEAR', l.createdAt) = :year
         and function('MONTH', l.createdAt) = :month
       order by l.createdAt desc, l.logId desc
    """)
    Page<ApplyEmpAdminLog> findByJobIdAndYearMonthNotDeleted(
        @Param("jobId") Long jobId,
        @Param("year") int year,
        @Param("month") int month,
        Pageable pageable
    );

    /** 월별 건수 프로젝션 (삭제 제외) */
    interface YmCount {
        Integer getY();
        Integer getM();
        Long getCnt();
    }

    @Query("""
      select year(l.createdAt) as y, month(l.createdAt) as m, count(l) as cnt
        from ApplyEmpAdminLog l
       where l.job.jobId = :jobId
         and l.deleted = false
       group by year(l.createdAt), month(l.createdAt)
       order by y desc, m desc
    """)
    List<YmCount> countByYearMonthNotDeleted(@Param("jobId") Long jobId);
}