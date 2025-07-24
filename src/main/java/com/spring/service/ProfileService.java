package com.spring.service;

import com.spring.dto.ProfileRegisterRequestDto;
import com.spring.dto.ProfileRegisterResponseDto;
import com.spring.dto.response.ProfileAdminResponseDto;
import com.spring.page.dto.ProfilePage;

import java.util.List;

public interface ProfileService {

    /**
     * 모든 프로필 조회 (관리자용 전체 리스트)
     */
    List<ProfileAdminResponseDto> getAllProfiles();

    /**
     * 페이징 처리된 프로필 리스트 조회 (기본 리스트)
     */
    ProfilePage getPagedProfiles(int page);

    /**
     * 조건 필터 기반 페이징 처리된 프로필 리스트 조회
     */
    ProfilePage getFileProfilePage(
            int page,
            String desiredLocation,
            String nationality,
            String gender,
            String visaType,
            String keyword,
            String nationalityType,
            List<String> excludeNationalities
    );

    /**
     * 프로필 등록
     */
    Long saveProfile(ProfileRegisterRequestDto profileDto);

    /**
     * 프로필 상세 정보 조회 (수정용)
     */
    ProfileRegisterResponseDto getProfileForUpdate(Long id);

    /**
     * 프로필 삭제
     */
    void deleteById(Long id);

    /**
     * 프로필 수정
     */
    void updateProfile(ProfileRegisterRequestDto profileDto);
}
