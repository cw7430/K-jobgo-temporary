package com.spring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @Column(name = "name_kor")
    private String nameKor;

    @Column(name = "name_origin")
    private String nameOrigin;

    @Column(name = "gender")
    private String gender;

    @Column(name = "visa_type")
    private String visaType;

    @Column(name = "visa_expire")
    private LocalDate visaExpire;

    @Column(name = "visa_extendable")
    private Boolean visaExtendable;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "driving_license")
    private Boolean drivingLicense;

    @Column(name = "dormitory")
    private Boolean dormitory;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private PersonalInfo personalInfo;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> educationList;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkExperience> workList;
}
