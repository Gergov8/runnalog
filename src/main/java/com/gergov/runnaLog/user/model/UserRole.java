package com.gergov.runnaLog.user.model;

public enum UserRole {
    ADMIN ("Admin"),
    USER ("User");

    private String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
