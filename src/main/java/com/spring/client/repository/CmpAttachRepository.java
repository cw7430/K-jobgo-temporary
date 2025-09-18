package com.spring.client.repository;

import com.spring.client.entity.CmpAttach;
import com.spring.client.enums.FileCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CmpAttachRepository extends JpaRepository<CmpAttach, Long>{ // 첨부파일

    List<CmpAttach> findByCmpInfo_CmpId(Long cmpId);

    Optional<CmpAttach> findByCmpInfo_CmpIdAndFileCategory(Long cmpId, FileCategory fileCategory);

    boolean existsByCmpInfo_CmpIdAndFileCategory(Long cmpId, FileCategory fileCategory);

    long deleteByCmpInfo_CmpId(Long cmpId);
}
