package com.spring.client.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.client.dto.CmpAttachDto;
import com.spring.client.entity.CmpAttach;
import com.spring.client.entity.CmpInfo;
import com.spring.client.enums.FileCategory;
import com.spring.client.repository.CmpAttachRepository;
import com.spring.client.repository.CmpInfoRepository;
import com.spring.client.service.CmpAttachService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class CmpAttachServiceImpl implements CmpAttachService {

    private final CmpAttachRepository repo;
    private final CmpInfoRepository cmpInfoRepo; // cmpId 로 참조 엔티티 로딩용

    @Override
    public CmpAttachDto upsert(Long cmpId, FileCategory fileCategory, CmpAttachDto dto) {
        CmpInfo cmp = cmpInfoRepo.findById(cmpId)
            .orElseThrow(() -> new IllegalArgumentException("회사 없음: " + cmpId));

        CmpAttach entity = repo.findByCmpInfo_CmpIdAndFileCategory(cmpId, fileCategory)
            .map(a -> { // 업데이트
                a.setOrigName(dto.getOrigName());
                a.setFPath(dto.getFPath());
                a.setFExt(dto.getFExt());
                a.setFMime(dto.getFMime());
                a.setFSize(dto.getFSize());
                return a;
            })
            .orElseGet(() -> CmpAttach.builder() // 생성
                .cmpInfo(cmp)
                .fileCategory(fileCategory)
                .origName(dto.getOrigName())
                .fPath(dto.getFPath())
                .fExt(dto.getFExt())
                .fMime(dto.getFMime())
                .fSize(dto.getFSize())
                .build());

        CmpAttach saved = repo.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CmpAttachDto> getOne(Long cmpId, FileCategory category) {
        return repo.findByCmpInfo_CmpIdAndFileCategory(cmpId, category).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CmpAttachDto> list(Long cmpId) {
        return repo.findByCmpInfo_CmpId(cmpId).stream().map(this::toDto).toList();
    }

    @Override
    public void removeAllOfCompany(Long cmpId) {
        repo.deleteByCmpInfo_CmpId(cmpId);
    }

    private CmpAttachDto toDto(CmpAttach e) {
        return CmpAttachDto.builder()
            .fId(e.getFId())
            .cmpId(e.getCmpInfo().getCmpId())
            .fileCategory(e.getFileCategory())     
            .origName(e.getOrigName())
            .fPath(e.getFPath())
            .fExt(e.getFExt())
            .fMime(e.getFMime())
            .fSize(e.getFSize())
            .upldDt(e.getUpldDt())
            .build();
    }
}
