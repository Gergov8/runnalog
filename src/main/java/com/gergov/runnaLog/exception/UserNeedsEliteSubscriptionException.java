package com.gergov.runnaLog.exception;

import org.springframework.security.authorization.AuthorizationDeniedException;

public class UserNeedsEliteSubscriptionException extends AuthorizationDeniedException {

    public UserNeedsEliteSubscriptionException(String msg) {

        super(msg);
    }
}
