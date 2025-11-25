package com.gergov.runnaLog.exception;

public class UserEmailAlreadyExistsException extends RuntimeException {

    public UserEmailAlreadyExistsException() {
    }

    public UserEmailAlreadyExistsException(String message) {
        super(message);
    }
}
