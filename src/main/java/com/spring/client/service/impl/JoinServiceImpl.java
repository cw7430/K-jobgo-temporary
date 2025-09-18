package com.spring.client.service.impl;

import com.spring.client.dto.request.JoinRequestDTO;
import com.spring.client.entity.*;
import com.spring.client.enums.FileCategory;
import com.spring.client.enums.JobStatus;
import com.spring.client.event.RegistrationSubmittedEvent;
import com.spring.client.repository.*;
import com.spring.client.service.EmailService;
import com.spring.client.service.JoinService;
import com.spring.service.FileService;

import java.io.IOException;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class JoinServiceImpl implements JoinService {
        
    private final CmpInfoRepository         infoRepo;
    private final CmpContRepository         contRepo;
    private final CmpAttachRepository       attachRepo;
    private final CmpJobConditionRepository jobRepo;
    private final PasswordEncoder           passwordEncoder;
    private final FileService               fileService;
    private final ApplicationEventPublisher publisher;

    private static final Set<String> ALLOWED_EXTS = Set.of("pdf","jpg","jpeg","png");
    private static final long MAX_BYTES = 20 * 1024 * 1024;

    
    @Override
    public boolean existsBizNo(String bizNo) {
        return infoRepo.existsByBizNoAndIsDelFalse(bizNo);  
    }

    @Override
    public boolean existsEmail(String email) {
        return infoRepo.existsByBizEmail(email);
    }

    @Override
    public Long register(JoinRequestDTO dto) {
        log.info("[JOIN] start email={}", dto.getBizEmail());
        
        if (!Boolean.TRUE.equals(dto.getFileConfirm())) {
            throw new IllegalArgumentException("첨부파일 확인 동의가 필요합니다.");
        }

        // (1) 파일 선검증
        if (dto.getBizFileLicense() == null || dto.getBizFileLicense().isEmpty())
            throw new IllegalArgumentException("사업자등록증 파일은 필수입니다.");
        if (dto.getBizFileCard() == null || dto.getBizFileCard().isEmpty())
            throw new IllegalArgumentException("명함 파일은 필수입니다.");

        // (2) 회사 기본정보
        CmpInfo cmpInfo = infoRepo.save(
            CmpInfo.builder()
                .cmpName(dto.getCmpName())
                .ceoName(dto.getCeoName())
                .bizNo(dto.getBizNo()) // 컨트롤러에서 normalize 해서 들어옴
                .bizEmail(dto.getBizEmail())
                .bizPwd(passwordEncoder.encode(dto.getBizPwd()))
                .zipCode(dto.getZipCode())
                .cmpAddr(dto.getCmpAddr())
                .addrDt(dto.getAddrDt())
                .cmpPhone(dto.getCmpPhone())
                .prxJoin(Boolean.TRUE.equals(dto.getPrxJoin()))
                .proxyExecutor(dto.getProxyExecutor())
                .fileConfirm(Boolean.TRUE.equals(dto.getFileConfirm()))
                .agrTerms(Boolean.TRUE.equals(dto.getAgrTerms()))
                .build()
        );

        // (3) 담당자
        contRepo.save(
            CmpCont.builder()
                .cmpInfo(cmpInfo)
                .empName(dto.getEmpName())
                .empTitle(dto.getEmpTitle())
                .empPhone(dto.getEmpPhone())
                .build()
        );

        // (4) 첨부
        saveAttachment(cmpInfo, dto.getBizFileLicense(), FileCategory.BUSINESS_LICENSE);
        saveAttachment(cmpInfo, dto.getBizFileCard(),    FileCategory.BUSINESS_CARD);

        // (5) (선택) 구인조건
        if (dto.getJobType() != null && !dto.getJobType().isBlank()) {
            jobRepo.save(
                CmpJobCondition.builder()
                    .cmpInfo(cmpInfo)
                    .jobType(dto.getJobType())
                    .desiredNationality(dto.getDesiredNationality())
                    .desiredCount(dto.getDesiredCount())
                    .jobCategory(dto.getJobCategory())
                    .experience(dto.getExperience())
                    .education(dto.getEducation())
                    .qualification(dto.getQualification())
                    .workingHours(dto.getWorkingHours())
                    .breakTime(dto.getBreakTime())
                    .employmentType(dto.getEmploymentType())
                    .insurance(dto.getInsurance())
                    .retirementPay(dto.getRetirementPay())
                    .totalCount(dto.getTotalCount())
                    .currentForeigners(dto.getCurrentForeigners())
                    .dormitory(dto.getDormitory())
                    .meal(dto.getMeal())
                    .jobDescription(dto.getJobDescription())
                    .major(dto.getMajor())
                    .computerSkills(dto.getComputerSkills())
                    .languageSkills(dto.getLanguageSkills())
                    .preferredConditions(dto.getPreferredConditions())
                    .otherPreferredConditions(dto.getOtherPreferredConditions())
                    .otherNotes(dto.getOtherNotes())
                    .status(JobStatus.ACTIVE)
                    .build()
            );
        }

        // (6) 이벤트 발행
        publisher.publishEvent(new RegistrationSubmittedEvent(
            cmpInfo.getCmpId(), cmpInfo.getBizEmail(), dto
        ));

        return cmpInfo.getCmpId();
    }

    private void saveAttachment(CmpInfo info, MultipartFile file, FileCategory cat) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException(cat + " 파일은 필수입니다.");
        validateFile(file);

        try {
            String storageKey = fileService.upload(file); // ← 키만 저장
            var opt = attachRepo.findByCmpInfo_CmpIdAndFileCategory(info.getCmpId(), cat);

            CmpAttach entity = opt.map(a -> {
                a.setOrigName(file.getOriginalFilename());
                a.setFPath(storageKey);                 // ← 키 저장
                a.setFExt(getExtension(file));
                a.setFMime(file.getContentType());
                a.setFSize(file.getSize());
                return a;
            }).orElseGet(() -> CmpAttach.builder()
                .cmpInfo(info)
                .fileCategory(cat)
                .origName(file.getOriginalFilename())
                .fPath(storageKey)                      // ← 키 저장
                .fExt(getExtension(file))
                .fMime(file.getContentType())
                .fSize(file.getSize())
                .build()
            );

            attachRepo.save(entity);

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("파일 용량은 20MB를 초과할 수 없습니다.");
        }
        String ext = getExtension(file).toLowerCase();
        if (!ALLOWED_EXTS.contains(ext)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + ext);
        }
    }

    private String getExtension(MultipartFile file) {
        String name = file.getOriginalFilename();
        return (name != null && name.contains(".")) ? name.substring(name.lastIndexOf('.') + 1) : "";
    }
}