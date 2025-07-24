package com.spring.repository;

import com.spring.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    // 기본 전체 조회 (페이징 포함)
    Page<Profile> findAll(Pageable pageable);

    // 조건 기반 필터 검색 (페이징 + countQuery 포함)
    @Query(
        value = """
            SELECT p FROM Profile p
            LEFT JOIN p.personalInfo pi
            WHERE
                (
                    (
                        (:nationalityType = 'etc' AND (pi.nationality IS NULL OR pi.nationality NOT IN :excludeNationalities))
                        OR
                        (:nationalityType IS NULL AND :nationality IS NOT NULL AND pi.nationality LIKE CONCAT('%', :nationality, '%'))
                        OR
                        (:nationality IS NULL AND :nationalityType IS NULL)
                    )
                )
                AND (:desiredLocation IS NULL OR pi.desiredLocation IS NULL OR pi.desiredLocation LIKE CONCAT('%', :desiredLocation, '%'))
                AND (:gender IS NULL OR p.gender LIKE CONCAT('%', :gender, '%'))
                AND (:visaType IS NULL OR p.visaType LIKE CONCAT('%', :visaType, '%'))
                AND (
                    :keyword IS NULL OR
                    p.nameKor LIKE CONCAT('%', :keyword, '%') OR
                    p.nameOrigin LIKE CONCAT('%', :keyword, '%') OR
                    p.strengths LIKE CONCAT('%', :keyword, '%') OR
                    pi.nationality LIKE CONCAT('%', :keyword, '%') OR
                    pi.desiredLocation IS NULL OR pi.desiredLocation LIKE CONCAT('%', :keyword, '%') OR
                    pi.topikLevel IS NULL OR pi.topikLevel LIKE CONCAT('%', :keyword, '%')
                )
        """,
        countQuery = """
            SELECT COUNT(p) FROM Profile p
            LEFT JOIN p.personalInfo pi
            WHERE
                (
                    (
                        (:nationalityType = 'etc' AND (pi.nationality IS NULL OR pi.nationality NOT IN :excludeNationalities))
                        OR
                        (:nationalityType IS NULL AND :nationality IS NOT NULL AND pi.nationality LIKE CONCAT('%', :nationality, '%'))
                        OR
                        (:nationality IS NULL AND :nationalityType IS NULL)
                    )
                )
                AND (:desiredLocation IS NULL OR pi.desiredLocation IS NULL OR pi.desiredLocation LIKE CONCAT('%', :desiredLocation, '%'))
                AND (:gender IS NULL OR p.gender LIKE CONCAT('%', :gender, '%'))
                AND (:visaType IS NULL OR p.visaType LIKE CONCAT('%', :visaType, '%'))
                AND (
                    :keyword IS NULL OR
                    p.nameKor LIKE CONCAT('%', :keyword, '%') OR
                    p.nameOrigin LIKE CONCAT('%', :keyword, '%') OR
                    p.strengths LIKE CONCAT('%', :keyword, '%') OR
                    pi.nationality LIKE CONCAT('%', :keyword, '%') OR
                    pi.desiredLocation IS NULL OR pi.desiredLocation LIKE CONCAT('%', :keyword, '%') OR
                    pi.topikLevel IS NULL OR pi.topikLevel LIKE CONCAT('%', :keyword, '%')
                )
        """
    )
    Page<Profile> findByFilters(
        @Param("desiredLocation") String desiredLocation,
        @Param("nationality") String nationality,
        @Param("gender") String gender,
        @Param("visaType") String visaType,
        @Param("keyword") String keyword,
        @Param("nationalityType") String nationalityType,
        @Param("excludeNationalities") List<String> excludeNationalities,
        Pageable pageable
    );
}
