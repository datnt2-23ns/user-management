package org.example.usermanagement.exception;

public class InvalidPasswordChangeException extends RuntimeException {

    public InvalidPasswordChangeException(String message) {
        super(message);
    }
}