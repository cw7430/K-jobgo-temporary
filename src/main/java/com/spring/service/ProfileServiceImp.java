package com.spring.service;

import com.spring.dto.ProfileRegisterRequestDto;
import com.spring.dto.ProfileRegisterResponseDto;
import com.spring.dto.request.EducationRequestDto;
import com.spring.dto.request.PersonalInfoRequestDto;
import com.spring.dto.request.ProfileRequestDto;
import com.spring.dto.request.WorkExperienceRequestDto;
import com.spring.dto.response.EducationAdminResponseDto;
import com.spring.dto.response.PersonalInfoAdminResponseDto;
import com.spring.dto.response.ProfileAdminResponseDto;
import com.spring.dto.response.WorkExperienceAdminResponseDto;
import com.spring.entity.PersonalInfo;
import com.spring.entity.Profile;
import com.spring.page.dto.ProfilePage;
import com.spring.repository.EducationRepository;
import com.spring.repository.PersonalInfoRepository;
import com.spring.repository.ProfileRepository;
import com.spring.repository.WorkExperienceRepository;
import com.spring.service.ProfileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.Generated;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProfileServiceImp implements ProfileService {

    private final ProfileRepository profileRepository;
    private final PersonalInfoRepository personalInfoRepository;
    private final EducationRepository educationRepository;
    private final WorkExperienceRepository workExperienceRepository;

    @Generated
    public ProfileServiceImp(ProfileRepository profileRepository,
                             PersonalInfoRepository personalInfoRepository,
                             EducationRepository educationRepository,
                             WorkExperienceRepository workExperienceRepository) {
        this.profileRepository = profileRepository;
        this.personalInfoRepository = personalInfoRepository;
        this.educationRepository = educationRepository;
        this.workExperienceRepository = workExperienceRepository;
    }

    @Transactional
    public Long saveProfile(ProfileRegisterRequestDto registerDto) {
        Profile profile = registerDto.getProfile().toEntity();
        profile.setStrengths(registerDto.getStrengths());
        profile = profileRepository.save(profile);

        PersonalInfoRequestDto personalInfoDto = registerDto.getPersonalInfo();
        System.out.println("✅ personalInfoDto: " + personalInfoDto); // toString()이 없으면 null 또는 클래스명 출력

        if (personalInfoDto != null) {
            System.out.println("✅ nationality: " + personalInfoDto.getNationality());
            System.out.println("✅ age: " + personalInfoDto.getAge());
            System.out.println("✅ height: " + personalInfoDto.getHeight());
            System.out.println("✅ weight: " + personalInfoDto.getWeight());
            System.out.println("✅ desiredLocation: " + personalInfoDto.getDesiredLocation());

            PersonalInfo personalInfo = personalInfoDto.toEntity(profile);
            personalInfo.setProfile(profile);
            profile.setPersonalInfo(personalInfo);

            personalInfoRepository.save(personalInfo);
            System.out.println("✅ Saved personalInfo ID: " + personalInfo.getPersonalId());
            personalInfoRepository.saveAndFlush(personalInfo);
        } else {
            System.out.println("❌ personalInfoDto is null");
        }

        List<EducationRequestDto> educationDto = registerDto.getEducationList();
        if (educationDto != null) {
            for (EducationRequestDto ed : educationDto) {
                educationRepository.save(ed.toEntity(profile));
            }
        }

        List<WorkExperienceRequestDto> workDto = registerDto.getWorkList();
        if (workDto != null) {
            for (WorkExperienceRequestDto wk : workDto) {
                workExperienceRepository.save(wk.toEntity(profile));
            }
        }

        System.out.println("✔ profileId: " + profile.getProfileId());
        System.out.println("✔ personalInfo: " + profile.getPersonalInfo());

        return profile.getProfileId();
    }

    public ProfileRegisterResponseDto getProfileForUpdate(Long id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로필이 존재하지 않습니다. ID: " + id));

        PersonalInfo personalInfo = personalInfoRepository.findByProfile(profile);
        System.out.println("✅ personalInfo: " + personalInfo);
        System.out.println("✅ personalInfo: " + personalInfo.toString());

        List<EducationAdminResponseDto> educationList = educationRepository.findByProfile(profile)
                .stream().map(EducationAdminResponseDto::fromEntity).toList();

        List<WorkExperienceAdminResponseDto> workList = workExperienceRepository.findByProfile(profile)
                .stream().map(WorkExperienceAdminResponseDto::fromEntity).toList();

        return new ProfileRegisterResponseDto(
                ProfileAdminResponseDto.fromEntity(profile, personalInfo),
                PersonalInfoAdminResponseDto.fromEntity(personalInfo),
                educationList,
                workList,
                profile.getStrengths()
        );
    }

    public List<ProfileAdminResponseDto> getAllProfiles() {
        List<Profile> profiles = profileRepository.findAll();
        return profiles.stream().map(profile -> {
            PersonalInfo personalInfo = personalInfoRepository.findByProfile(profile);
            return ProfileAdminResponseDto.fromEntity(profile, personalInfo);
        }).toList();
    }

    public ProfilePage getPagedProfiles(int page) {
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Profile> profilePage = profileRepository.findAll(pageable);
        List<ProfileAdminResponseDto> profileDtos = profilePage.getContent().stream().map(profile -> {
            PersonalInfo info = personalInfoRepository.findByProfile(profile);
            return ProfileAdminResponseDto.fromEntity(profile, info);
        }).toList();
        return new ProfilePage(page, (int) profilePage.getTotalElements(), pageSize, profileDtos);
    }

    public ProfilePage getFileProfilePage(int page, String desiredLocation, String nationality, String gender, String visaType, String keyword, String nationalityType, List<String> excludeNationalities) {
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Profile> profilePage = profileRepository.findByFilters(desiredLocation, nationality, gender, visaType, keyword, nationalityType, excludeNationalities, pageable);

        List<ProfileAdminResponseDto> profileDtos = profilePage.getContent().stream().map(profile -> {
            PersonalInfo info = personalInfoRepository.findByProfile(profile);
            return ProfileAdminResponseDto.fromEntity(profile, info);
        }).toList();

        return new ProfilePage(page, (int) profilePage.getTotalElements(), pageSize, profileDtos);
    }

    @Transactional
    public void updateProfile(ProfileRegisterRequestDto requestDto) {
        if (requestDto.getProfile() == null) {
            throw new RuntimeException("profile이 null입니다");
        }

        Profile profile = profileRepository.findById(requestDto.getProfile().getProfileId())
                .orElseThrow(() -> new IllegalArgumentException("해당 프로필이 존재하지 않습니다."));

        ProfileRequestDto profileDto = requestDto.getProfile();
        profile.setNameKor(profileDto.getNameKor());
        profile.setNameOrigin(profileDto.getNameOrigin());
        profile.setGender(profileDto.getGender());
        profile.setVisaType(profileDto.getVisaType());
        profile.setVisaExpire(profileDto.getVisaExpire());
        profile.setVisaExtendable(profileDto.getVisaExtendable());
        profile.setEmail(profileDto.getEmail());
        profile.setPhone(profileDto.getPhone());
        profile.setAddress(profileDto.getAddress());
        profile.setDrivingLicense(profileDto.getDrivingLicense());
        profile.setDormitory(profileDto.getDormitory());
        profile.setPhotoUrl(profileDto.getPhotoUrl());
        profile.setStrengths(requestDto.getStrengths());

        PersonalInfoRequestDto infoDto = requestDto.getPersonalInfo();
        if (infoDto != null) {
            PersonalInfo existingInfo = personalInfoRepository.findByProfile(profile);
            if (existingInfo != null) {
                existingInfo.updateFromDto(infoDto);
            } else {
                personalInfoRepository.save(infoDto.toEntity(profile));
            }
        }

        educationRepository.deleteByProfile(profile);
        List<EducationRequestDto> newEduList = requestDto.getEducationList();
        if (newEduList != null) {
            for (EducationRequestDto ed : newEduList) {
                educationRepository.save(ed.toEntity(profile));
            }
        }

        workExperienceRepository.deleteByProfile(profile);
        List<WorkExperienceRequestDto> newWorkList = requestDto.getWorkList();
        if (newWorkList != null) {
            for (WorkExperienceRequestDto wk : newWorkList) {
                workExperienceRepository.save(wk.toEntity(profile));
            }
        }
    }

    @Transactional
    public void deleteById(Long id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 프로필이 존재하지 않습니다."));
        profileRepository.delete(profile);
    }
}
