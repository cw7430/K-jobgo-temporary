// src/main/java/com/spring/client/repository/ActionRequestRepository.java
package com.spring.client.repository;

import com.spring.client.entity.ActionRequest;
import com.spring.client.enums.ApprStatus;
import com.spring.client.enums.ReqType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionRequestRepository extends JpaRepository<ActionRequest, Long> {

    // WITHDRAW 중복 PENDING 방지
    boolean existsByCmp_CmpIdAndReqTypeAndReqStatus(Long cmpId, ReqType type, ApprStatus status);

    // JOB_CANCEL 중복 PENDING 방지
    boolean existsByCmp_CmpIdAndJob_JobIdAndReqTypeAndReqStatus(
            Long cmpId, Long jobId, ReqType type, ApprStatus status
    );
}
