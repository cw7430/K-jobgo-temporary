package com.spring.dto.request;

import com.spring.entity.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileRequestDto {
    private Long profileId;
    private String nameKor;
    private String nameOrigin;
    private String gender;
    private String visaType;
    private LocalDate visaExpire;
    private Boolean visaExtendable;
    private String emailId;
    private String emailDomain;
    private String phone1;
    private String phone2;
    private String phone3;
    private String address;
    private Boolean drivingLicense;
    private Boolean dormitory;
    private String photoUrl;
    private MultipartFile photo;
    private String strengths;

    public String getEmail() {
        if (emailId != null && emailDomain != null) {
            return emailId + "@" + emailDomain;
        }
        return null;
    }

    public String getPhone() {
        if (phone1 != null && phone2 != null && phone3 != null) {
            return phone1 + "-" + phone2 + "-" + phone3;
        }
        return null;
    }

    public Profile toEntity() {
        return Profile.builder()
                .nameKor(this.nameKor)
                .nameOrigin(this.nameOrigin)
                .gender(this.gender)
                .visaType(this.visaType)
                .visaExpire(this.visaExpire)
                .visaExtendable(this.visaExtendable)
                .email(getEmail())
                .phone(getPhone())
                .address(this.address)
                .drivingLicense(this.drivingLicense)
                .dormitory(this.dormitory)
                .photoUrl(this.photoUrl)
                .build();
    }
}
