package org.example.usermanagement.exception;

public class InvalidUserListQueryException
        extends RuntimeException {

    public InvalidUserListQueryException(
            String message
    ) {
        super(message);
    }
}