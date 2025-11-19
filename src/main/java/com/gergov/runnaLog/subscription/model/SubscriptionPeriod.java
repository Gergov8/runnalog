package com.gergov.runnaLog.subscription.model;

import lombok.Getter;

@Getter
public enum SubscriptionPeriod {
    MONTHLY("Monthly"),
    YEARLY("Yearly"),;

    private final String displayName;

    SubscriptionPeriod(String displayName) {
        this.displayName = displayName;
    }

}
