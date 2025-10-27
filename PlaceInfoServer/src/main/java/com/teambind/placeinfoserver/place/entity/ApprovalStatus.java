package com.teambind.placeinfoserver.place.entity;

/**
 * 업체 승인 상태 Enum
 */
public enum ApprovalStatus {
    PENDING("승인 대기"),
    APPROVED("승인 완료"),
    REJECTED("승인 거부");

    private final String description;

    ApprovalStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
