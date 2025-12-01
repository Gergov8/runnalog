package com.gergov.runnaLog.exception;

import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationResult;

public class UserNeedsEliteSubscriptionException extends AuthorizationDeniedException {
    public UserNeedsEliteSubscriptionException(String msg, AuthorizationResult authorizationResult) {
        super(msg, authorizationResult);
    }

    public UserNeedsEliteSubscriptionException(String msg) {
        super(msg);
    }
}
