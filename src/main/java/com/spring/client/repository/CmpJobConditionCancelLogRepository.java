package com.spring.client.repository;

import com.spring.client.entity.CmpJobConditionCancelLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CmpJobConditionCancelLogRepository
        extends JpaRepository<CmpJobConditionCancelLog, Long> { // 삭제, 취소 로그

    // 해당 구인조건의 삭제 이력 최신순
	  List<CmpJobConditionCancelLog> findByJob_JobIdOrderByCancelledAtDesc(Long jobId);
}