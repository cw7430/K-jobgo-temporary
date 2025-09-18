package com.spring.client.repository;

import com.spring.client.entity.CmpCont;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CmpContRepository extends JpaRepository<CmpCont, Long> { // 담당자 정보

    List<CmpCont> findByCmpInfo_CmpId(Long cmpId);

    long deleteByCmpInfo_CmpId(Long cmpId);
    
    Optional<CmpCont> findTopByCmpInfo_CmpIdOrderByEmpIdDesc(Long cmpId);
}
