package com.spring.client.entity;

import com.spring.client.enums.EmailStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cmp_appr_hist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmpApprHist {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long apprId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cmp_id", nullable = false)
	private CmpInfo cmpInfo;

	@Column(name = "is_appr", nullable = false)
	private boolean isAppr;

	@Column(name = "appr_dt")
	private LocalDateTime apprDt;

	@Column(name = "appr_by", length = 100)
	private String apprBy;

	@Column(name = "appr_cmt", columnDefinition = "TEXT")
	private String apprCmt;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(name = "email_status", nullable = false, length = 10)
	private EmailStatus emailStatus = EmailStatus.PENDING;

	@Column(name = "email_sent_at")
	private LocalDateTime emailSentAt;

	@Column(name = "email_error_msg", columnDefinition = "TEXT")
	private String emailErrorMsg;

	@Column(name = "crt_dt", nullable = false, updatable = false)
	private LocalDateTime crtDt;

	@Column(name = "upd_dt", nullable = false)
	private LocalDateTime updDt;

	@PrePersist
	private void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.crtDt = now;
		this.updDt = now;
	}

	@PreUpdate
	private void onUpdate() {
		this.updDt = LocalDateTime.now();
	}
}