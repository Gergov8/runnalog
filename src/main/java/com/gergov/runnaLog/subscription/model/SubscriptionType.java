package com.gergov.runnaLog.subscription.model;

import lombok.Getter;

@Getter
public enum SubscriptionType {
    RECREATIONAL ("Recreational"),
    COMPETITIVE ("Competitive"),
    ELITE ("Elite"),;

    private final String displayName;

    SubscriptionType(String displayName) {
        this.displayName = displayName;
    }

}
