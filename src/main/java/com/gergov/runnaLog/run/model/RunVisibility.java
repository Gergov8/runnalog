package com.gergov.runnaLog.run.model;

import lombok.Getter;

@Getter
public enum RunVisibility {
    PUBLIC ("Public"),
    PRIVATE ("Private");

    private final String displayName;

    RunVisibility(String displayName) {
        this.displayName = displayName;
    }

}
