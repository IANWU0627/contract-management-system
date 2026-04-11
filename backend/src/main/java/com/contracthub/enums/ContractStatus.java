package com.contracthub.enums;

public enum ContractStatus {
    DRAFT("草稿"),
    PENDING_APPROVAL("待审批"),
    APPROVING("审批中"),
    APPROVED("已批准"),
    SIGNED("已签署"),
    ARCHIVED("已归档"),
    TERMINATED("已终止"),
    REJECTED("已拒绝");

    private final String description;

    ContractStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
