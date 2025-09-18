package com.spring.client.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class CmpJobConditionCancelLogDto {
    private Long logId;                 // log_id
    private Long jobId;                 // job_id
    private String cancelReason;        // cancel_reason
    private String cancelledBy;         // cancelled_by
    private LocalDateTime cancelledAt;  // cancelled_at
}
