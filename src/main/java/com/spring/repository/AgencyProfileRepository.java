package com.spring.repository;

import com.spring.entity.AgencyProfile;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface AgencyProfileRepository extends JpaRepository<AgencyProfile, Long> {

    // 전체 조회도 deleted=false만
    @Query("""
        SELECT p FROM AgencyProfile p
        WHERE p.deleted = false
        ORDER BY p.createdAt DESC
    """)
    Page<AgencyProfile> findAllActive(Pageable pageable);

    @Query(
    		  value = """
    		    SELECT p FROM AgencyProfile p
    		    WHERE p.deleted = false AND
    		          LOWER(CONCAT(
    		              COALESCE(p.agencyName,''), ' ',
    		              COALESCE(p.visaType,''), ' ',
    		              COALESCE(p.jobCode,''), ' ',
    		              COALESCE(p.employeeNameEn,''), ' ',
    		              COALESCE(p.nationalityEn,'')
    		          )) LIKE CONCAT('%', :kw, '%')
    		    ORDER BY p.createdAt DESC
    		  """,
    		  countQuery = """
    		    SELECT COUNT(p) FROM AgencyProfile p
    		    WHERE p.deleted = false AND
    		          LOWER(CONCAT(
    		              COALESCE(p.agencyName,''), ' ',
    		              COALESCE(p.visaType,''), ' ',
    		              COALESCE(p.jobCode,''), ' ',
    		              COALESCE(p.employeeNameEn,''), ' ',
    		              COALESCE(p.nationalityEn,'')
    		          )) LIKE CONCAT('%', :kw, '%')
    		  """
    		)
    		Page<AgencyProfile> searchByKeyword(@Param("kw") String keyword, Pageable pageable);
    
    @Query("""
    		  SELECT DISTINCT p FROM AgencyProfile p
    		  LEFT JOIN FETCH p.files
    		  WHERE p.deleted = false AND p.profileId = :id
    		""")
    		java.util.Optional<AgencyProfile> findWithFilesById(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update AgencyProfile p
         set p.deleted = true
       where p.profileId in :ids
         and p.deleted = false
    """)
    int softDeleteAllByIds(@Param("ids") List<Long> ids);

}

