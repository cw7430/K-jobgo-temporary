package com.spring.client.dto;

import com.spring.client.enums.ApprStatus;

public class ActionDecisionDTO {
    private Long reqId;
    private ApprStatus newStatus; // APPROVED or REJECTED
    private String decisionNote;
    private String decidedBy;
}