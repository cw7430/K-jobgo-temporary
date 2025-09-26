// 뷰용 응답 DTO
package com.spring.client.dto;

import lombok.*;
import java.time.LocalDateTime;

import com.spring.client.entity.ApplyEmpAdminLog;
import com.spring.client.enums.JobStatus;

@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class ApplyEmpAdminLogViewDTO {
    private Long logId;
    private String handledBy;
    private String counselContent;
    private String referenceNote;
    private JobStatus statusSnapshot;
    private Long writerAdminId;
    private String writerName;

    private LocalDateTime createdAt;
    
    // 상태 표시용
    private String statusSnapshotName;   // 예: "IN_PROGRESS"
    private String statusSnapshotLabel;  // 예: "진행중"
    
    private boolean deleted;
    private LocalDateTime deletedAt;
    private String deleteReason;
    private String deletedByName;

    public static ApplyEmpAdminLogViewDTO from(ApplyEmpAdminLog e) {
        return ApplyEmpAdminLogViewDTO.builder()
            .logId(e.getLogId())
            .handledBy(e.getHandledBy())
            .counselContent(e.getCounselContent())
            .statusSnapshot(e.getStatusSnapshot())
            .referenceNote(e.getReferenceNote())
            .writerAdminId(e.getWriterAdmin() != null ? e.getWriterAdmin().getAdminId() : null)
            .writerName(e.getWriterName())
            .createdAt(e.getCreatedAt())
            .statusSnapshotName(e.getStatusSnapshot() != null ? e.getStatusSnapshot().name() : null)
            .statusSnapshotLabel(e.getStatusSnapshot() != null ? e.getStatusSnapshot().getLabelKo() : null)
            .deleted(e.isDeleted())
            .deletedAt(e.getDeletedAt())
            .deleteReason(e.getDeleteReason())
            .deletedByName(e.getDeletedBy()!=null ? e.getDeletedBy().getAdminName() : null)
            .build();
    }
}
