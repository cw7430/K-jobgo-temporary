package com.spring.service;

import com.spring.dto.request.AgencyProfileRequestDto;
import com.spring.dto.response.AgencyRowDto;
import com.spring.entity.AgencyProfile;
import com.spring.entity.AgencyProfileFile;
import com.spring.entity.ProfileStatus;
import com.spring.repository.AgencyProfileRepository;
import com.spring.service.dto.AgencyDownloadPackage;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgencyProfileServiceImpl implements AgencyProfileService {

    private final AgencyProfileRepository agencyProfileRepository;
    private final FileService fileService;

    /** 등록 */
    @Override
    @Transactional
    public Long registerAgency(AgencyProfileRequestDto agencyProfiledto) {
        AgencyProfile profile = new AgencyProfile();
        profile.setAgencyName(agencyProfiledto.getAgencyName());
        profile.setVisaType(agencyProfiledto.getVisaType());
        profile.setJobCode(agencyProfiledto.getJobCode());
        profile.setEmployeeNameEn(agencyProfiledto.getEmployeeNameEn());
        profile.setNationalityEn(agencyProfiledto.getNationalityEn());
        profile.setStatus(ProfileStatus.READY);

        AgencyProfile saved = agencyProfileRepository.save(profile);

        
        // ✅ 단일 파일 처리 (반복문 X)
        MultipartFile f = agencyProfiledto.getFile();
        if (f != null && !f.isEmpty()) {
            String storage = fileServiceUpload(f);

            AgencyProfileFile pf = new AgencyProfileFile();
            pf.setOriginalName(f.getOriginalFilename());
            pf.setStorageKey(storage);
            pf.setMimeType(f.getContentType());
            pf.setFileSize(f.getSize());

            // 양방향 헬퍼 (pf.setProfile(saved) 포함되어 있어야 함)
            saved.addFile(pf);
        } else {
            // 컨트롤러에서 이미 검증하지만, 서비스 계층에서도 한 번 더 방어하고 싶다면 예외 처리
            throw new IllegalArgumentException("이력서 파일은 필수입니다.");
        }

        /* 다중파일 첨부 기능
        if (agencyProfiledto.getFiles() != null) {
            for (MultipartFile f : agencyProfiledto.getFiles()) {
                if (f == null || f.isEmpty()) continue;

                String storage = fileServiceUpload(f);
                AgencyProfileFile pf = new AgencyProfileFile();
                pf.setOriginalName(f.getOriginalFilename());
                pf.setStorageKey(storage);
                pf.setMimeType(f.getContentType());
                pf.setFileSize(f.getSize());

                // ✅ 양방향 헬퍼 사용 (연관관계 일관성)
                saved.addFile(pf);
            }
        } */
        return saved.getProfileId();
    }

    private String fileServiceUpload(MultipartFile f) {
        try {
            return fileService.upload(f);
        } catch (IOException e) {
            throw new RuntimeException("File upload failed: " + f.getOriginalFilename(), e);
        }
    }

    /** 페이지 조회 (키워드 검색 + 페이징) */
    @Override
    @Transactional(readOnly = true)
    public Page<AgencyRowDto> findAgencyPage(String keyword, Pageable pageable) {
        String kw = (keyword == null) ? "" : keyword.trim().toLowerCase();

        Page<AgencyProfile> page = kw.isEmpty()
                ? agencyProfileRepository.findAllActive(pageable)              // ✅ 아래 레포 추가
                : agencyProfileRepository.searchByKeyword(kw, pageable);       // ✅ deleted=false 포함

        // 엔티티 → 행 DTO 매핑
        return page.map(this::toRowDto);
    }

    private AgencyRowDto toRowDto(AgencyProfile p) {
        return AgencyRowDto.builder()
                .profileId(p.getProfileId())
                .agencyName(p.getAgencyName())
                .visaType(p.getVisaType())
                .jobCode(p.getJobCode())
                .employeeNameEn(p.getEmployeeNameEn())
                .nationalityEn(p.getNationalityEn())
                .statusLabel(p.getStatus() != null ? p.getStatus().getLabel() : "미배정") // ✅ 단순화
                .createdAt(p.getCreatedAt())
                .build();
    }

    /** 배정/다운로드 처리: 권한 1,2,5 공통 */
    @Override
    @Transactional
    public String assignAndReturnStatus(Long id, Integer authorityId, Long currentAdminId) {
        AgencyProfile p = agencyProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("대상이 존재하지 않습니다. id=" + id));

        if (p.isDeleted()) {
            throw new IllegalStateException("삭제된 항목입니다. id=" + id);
        }

        if (p.getStatus() != ProfileStatus.ASSIGNED) {
            p.setStatus(ProfileStatus.ASSIGNED);
            // 필요시 p.setAssignedBy(currentAdminId); p.setAssignedAt(...)
        }
        return p.getStatus().getLabel(); // ✅ enum 라벨 직접 사용
    }

    /** 삭제: 1,2만 가능(컨트롤러에서 1차 체크됨) */
    @Override
    @Transactional
    public void deleteById(Long id) {
        // 하드 삭제 그대로 유지하거나, 소프트 삭제를 원하면 아래로 변경:
        agencyProfileRepository.deleteById(id);

        /* 소프트 삭제로 바꾸려면:
        AgencyProfile p = agencyProfileRepository.findById(id).orElse(null);
        if (p != null) {
            p.setDeleted(true);
        }
        */
    }
    
    @Override
    @Transactional(readOnly = true)
    public AgencyDownloadPackage buildDownloadPackage(Long profileId) {
        AgencyProfile p = agencyProfileRepository.findWithFilesById(profileId)
            .orElseThrow(() -> new IllegalArgumentException("대상이 존재하지 않습니다. id=" + profileId));
        if (p.isDeleted()) throw new IllegalStateException("삭제된 항목입니다. id=" + profileId);

        var files = p.getFiles();
        if (files == null || files.isEmpty()) return null;
        if (files.size() != 1) throw new IllegalStateException("정책상 이력서는 1개만 허용됩니다. id=" + profileId);

        var f = files.get(0);
        try {
            var in  = fileService.download(f.getStorageKey());
            var len = (f.getFileSize() != null) ? f.getFileSize() : fileService.contentLength(f.getStorageKey());
            var mt  = fileService.guessMediaType(f.getMimeType(), f.getOriginalName());
            return new AgencyDownloadPackage(in, f.getOriginalName(), mt, len, true);
        } catch (IOException e) {
            throw new RuntimeException("파일 읽기 실패: " + f.getOriginalName(), e);
        }
    }

    /* 다중파일 첨부 기능
    @Override
    @Transactional(readOnly = true)
    public AgencyDownloadPackage buildDownloadPackage(Long profileId) {
        AgencyProfile p = agencyProfileRepository.findWithFilesById(profileId)
            .orElseThrow(() -> new IllegalArgumentException("대상이 존재하지 않습니다. id=" + profileId));
        if (p.isDeleted()) throw new IllegalStateException("삭제된 항목입니다. id=" + profileId);

        var files = p.getFiles();
        if (files == null || files.isEmpty()) return null;

        if (files.size() == 1) {
            var f = files.get(0);
            try {
                var in  = fileService.download(f.getStorageKey());
                var len = (f.getFileSize() != null) ? f.getFileSize() : fileService.contentLength(f.getStorageKey());
                var mt  = fileService.guessMediaType(f.getMimeType(), f.getOriginalName());
                return new AgencyDownloadPackage(in, f.getOriginalName(), mt, len, true);
            } catch (IOException e) {
                throw new RuntimeException("파일 읽기 실패: " + f.getOriginalName(), e);
            }
        }

        var entries = files.stream()
            .map(fe -> new FileService.FileEntry(fe.getStorageKey(), fe.getOriginalName()))
            .toList();
        try {
            var zipIn = fileService.buildZipStream(entries);
            return new AgencyDownloadPackage(zipIn, null, MediaType.valueOf("application/zip"), -1, false);
        } catch (IOException e) {
            throw new RuntimeException("ZIP 생성 실패", e);
        }
    }
    */
}
