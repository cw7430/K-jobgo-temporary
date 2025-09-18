package com.spring.client.service;

import java.util.List;
import java.util.Optional;

import com.spring.client.dto.CmpAttachDto;
import com.spring.client.enums.FileCategory;

public interface CmpAttachService {
    CmpAttachDto upsert(Long cmpId, FileCategory category, CmpAttachDto dto);
    Optional<CmpAttachDto> getOne(Long cmpId, FileCategory category);
    List<CmpAttachDto> list(Long cmpId);
    void removeAllOfCompany(Long cmpId);
}
