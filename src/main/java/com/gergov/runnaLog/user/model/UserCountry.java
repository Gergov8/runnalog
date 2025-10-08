package com.gergov.runnaLog.user.model;

public enum UserCountry {
    BULGARIA ("Bulgaria"),
    GERMANY ("Germany"),
    SPAIN ("Spain");

    private String displayName;

    UserCountry(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
