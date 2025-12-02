package com.gergov.runnaLog.exception;

public class UserEmailAlreadyExistsException extends RuntimeException {

    public UserEmailAlreadyExistsException(String message) {

        super(message);
    }
}
