package com.gergov.runnaLog.user.model;

import lombok.Getter;

@Getter
public enum UserCountry {
    BULGARIA ("Bulgaria"),
    GERMANY ("Germany"),
    SPAIN ("Spain");

    private final String displayName;

    UserCountry(String displayName) {
        this.displayName = displayName;
    }

}
