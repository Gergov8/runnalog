package com.gergov.runnaLog.trainingPlan.util;

import com.gergov.runnaLog.subscription.model.SubscriptionType;

public class PlanLevelConverter {

    public static String toPlanLevel(SubscriptionType subscriptionType) {

        if (subscriptionType == null) {
            return "RECREATIONAL";
        }

        return switch (subscriptionType) {
            case COMPETITIVE -> "COMPETITIVE";
            case ELITE -> "ELITE";
            default -> "RECREATIONAL";
        };
    }
}