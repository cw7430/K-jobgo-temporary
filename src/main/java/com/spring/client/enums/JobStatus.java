package com.spring.client.enums;

import java.util.List;

public enum JobStatus {
    ACTIVE("접수됨"),
    PENDING("검토중"),
    IN_PROGRESS("진행중"),
    ON_HOLD("보류"),
    COMPLETED("완료"),
    CANCELLED("취소"),
    REJECTED("반려");

    private final String labelKo;
    JobStatus(String labelKo){ this.labelKo = labelKo; }
    public String getLabelKo(){ return labelKo; }

    public List<JobStatus> nextAllowed() {
        return switch (this) {
            case ACTIVE      -> List.of(PENDING, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED, REJECTED);
            case PENDING     -> List.of(IN_PROGRESS, ON_HOLD, CANCELLED, REJECTED);
            case IN_PROGRESS -> List.of(COMPLETED, ON_HOLD, CANCELLED);
            case ON_HOLD     -> List.of(PENDING, IN_PROGRESS, CANCELLED);
            default          -> List.of(); // COMPLETED/CANCELLED/REJECTED는 종료
        };
    }
}