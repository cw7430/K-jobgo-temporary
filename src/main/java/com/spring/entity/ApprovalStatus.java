package com.spring.entity;

public enum ApprovalStatus {
    RECEIVED("비자접수중"),
    APPROVED("승인"),
    REJECTED("불허"),
    HOLD("보류");

    private final String label;
    ApprovalStatus(String label) { this.label = label; }
    public String label() { return label; }
}
