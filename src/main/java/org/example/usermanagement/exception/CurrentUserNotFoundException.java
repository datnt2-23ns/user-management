package org.example.usermanagement.exception;

public class CurrentUserNotFoundException
        extends RuntimeException {

    public CurrentUserNotFoundException(
            String message
    ) {
        super(message);
    }
}