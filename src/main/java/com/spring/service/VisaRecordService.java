package com.spring.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.spring.dto.request.VisaRecordRequestDto;
import com.spring.entity.VisaRecord;

public interface VisaRecordService {
  Long visaCreate(VisaRecordRequestDto visaDto, Long agentId);

  VisaRecordRequestDto getVisaRecordForm(Long id);

  Long register(VisaRecordRequestDto visaRecordRequestDto);

  void update(Long id, VisaRecordRequestDto visaRecordRequestDto);

  VisaRecord getEntity(Long id);

  List<VisaRecord> findRecent(int limit);

  void delete(Long id);

  Page<VisaRecord> search(String keyword, boolean includeDeleted, Pageable pageable);

  List<VisaRecord> findRecentActive(int limit);
  List<VisaRecord> findRecentIncludingDeleted(int limit);

  Page<VisaRecord> searchMine(String keyword, boolean includeDeleted, Pageable pageable);

}
