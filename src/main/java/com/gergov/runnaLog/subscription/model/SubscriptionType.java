package com.gergov.runnaLog.subscription.model;

public enum SubscriptionType {
    RECREATIONAL ("Recreational"),
    COMPETITIVE ("Competitive"),
    ELITE ("Elite"),;

    private String displayName;

    SubscriptionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
