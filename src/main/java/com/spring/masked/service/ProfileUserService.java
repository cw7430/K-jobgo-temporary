package com.spring.masked.service;

import com.spring.page.dto.ProfilePage;
import com.spring.profile.masked.dto.MaskedProfileDto;

import java.util.List;

public interface ProfileUserService {

    /**
     * 필터 조건에 따라 마스킹된 프로필 페이지 반환
     *
     * @param page 현재 페이지 번호 (1부터 시작)
     * @param size 페이지당 항목 수
     * @param desiredLocation 희망 근무지
     * @param nationality 국적
     * @param gender 성별
     * @param visaType 비자 유형
     * @param keyword 검색 키워드 (이름 등)
     * @param nationalityType 국적 타입 (예: 전체, 선호 국적 등)
     * @param excludeNationalities 제외할 국적 리스트
     * @return 필터링된 마스킹 프로필 페이지 객체
     */
    ProfilePage getMaskedProfilePage(
            int page,
            int size,
            String desiredLocation,
            String nationality,
            String gender,
            String visaType,
            String keyword,
            String nationalityType,
            List<String> excludeNationalities
    );

    /**
     * 프로필 상세 정보(마스킹된) 조회
     *
     * @param profileId 프로필 ID
     * @return 마스킹된 프로필 DTO
     */
    MaskedProfileDto getMaskedProfileDetail(Long profileId);
}
