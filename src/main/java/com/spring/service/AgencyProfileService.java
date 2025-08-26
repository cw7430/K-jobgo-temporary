package com.spring.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.spring.dto.request.AgencyProfileRequestDto;
import com.spring.dto.response.AgencyRowDto;
import com.spring.service.dto.AgencyDownloadPackage;

public interface AgencyProfileService {

	Long registerAgency(AgencyProfileRequestDto requestDto); // files는 dto에 선언

	// 송출업체 프로필 리스트 한 행 
	Page<AgencyRowDto> findAgencyPage(String keyword, Pageable pageable);

	// 배정, 다운로드 처리 api (권한 1,2,5만)
	String assignAndReturnStatus(Long id, Integer authorityId, Long currentAdminId);

	void deleteById(Long id);
	
	// 파일 다운로드
	AgencyDownloadPackage buildDownloadPackage(Long profileId);

}
