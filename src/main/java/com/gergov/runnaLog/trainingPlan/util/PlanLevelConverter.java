package com.gergov.runnaLog.trainingPlan.util;

import com.gergov.runnaLog.subscription.model.SubscriptionType;

public class PlanLevelConverter {

    public static String toPlanLevel(SubscriptionType subscriptionType) {
        if (subscriptionType == null) {
            return "RECREATIONAL";
        }

        // Convert SubscriptionType to the string value that microservice expects
        switch (subscriptionType) {
            case RECREATIONAL:
                return "RECREATIONAL";
            case COMPETITIVE:
                return "COMPETITIVE";
            case ELITE:
                return "ELITE";
            default:
                return "RECREATIONAL";
        }
    }

    public static SubscriptionType toSubscriptionType(String planLevel) {
        if (planLevel == null) {
            return SubscriptionType.RECREATIONAL;
        }

        switch (planLevel.toUpperCase()) {
            case "RECREATIONAL":
                return SubscriptionType.RECREATIONAL;
            case "COMPETITIVE":
                return SubscriptionType.COMPETITIVE;
            case "ELITE":
                return SubscriptionType.ELITE;
            default:
                return SubscriptionType.RECREATIONAL;
        }
    }
}