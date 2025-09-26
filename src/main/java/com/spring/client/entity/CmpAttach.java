package com.spring.client.entity;

import com.spring.client.enums.FileCategory;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "cmp_attach",
    uniqueConstraints = @UniqueConstraint(columnNames = {"cmp_id", "f_cat"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmpAttach { // 회원가입 첨부파일 영역

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cmp_id", nullable = false)
    private CmpInfo cmpInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "f_cat", nullable = false, length = 20)
    private FileCategory fileCategory;

    @Column(name = "orig_name", nullable = false, length = 255)
    private String origName;

    @Column(name = "f_path", nullable = false, length = 500)
    private String fPath;

    @Column(name = "f_ext", nullable = false, length = 10)
    private String fExt;

    @Column(name = "f_mime", nullable = false, length = 100)
    private String fMime;

    @Column(name = "f_size", nullable = false)
    private Long fSize;

    @Column(name = "upld_dt", nullable = false, updatable = false)
    private LocalDateTime upldDt;

    @PrePersist
    private void onCreate() {
        this.upldDt = LocalDateTime.now();
    }
}
