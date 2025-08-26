package com.spring.entity;

public enum ProfileStatus {
    READY("미배정"),       // 기본 등록된 상태
    ASSIGNED("배정완료");   // 다운로드 후 배정 완료

    private final String label; // 한글 표시용

    ProfileStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
