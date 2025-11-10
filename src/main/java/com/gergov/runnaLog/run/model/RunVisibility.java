package com.gergov.runnaLog.run.model;

public enum RunVisibility {
    PUBLIC ("Public"),
    PRIVATE ("Private");

    private String displayName;

    RunVisibility(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
