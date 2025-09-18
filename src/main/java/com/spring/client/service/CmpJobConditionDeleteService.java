package com.spring.client.service;

import com.spring.client.dto.CmpJobConditionCancelLogDto;
import java.util.List;

public interface CmpJobConditionDeleteService {
    CmpJobConditionCancelLogDto logCancel(CmpJobConditionCancelLogDto dto);
    List<CmpJobConditionCancelLogDto> findHistoryByJobId(Long jobId);
}
