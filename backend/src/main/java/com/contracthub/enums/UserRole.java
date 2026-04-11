package com.contracthub.enums;

public enum UserRole {
    ADMIN("管理员"),
    LEGAL("法务"),
    USER("普通用户");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
