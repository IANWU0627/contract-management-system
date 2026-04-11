package com.contracthub.enums;

public enum ContractType {
    PURCHASE("采购合同"),
    SALES("销售合同"),
    LEASE("租赁合同"),
    EMPLOYMENT("雇佣合同"),
    SERVICE("服务合同"),
    OTHER("其他合同");

    private final String description;

    ContractType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
