package org.example.usermanagement.exception;

public class AvatarStorageException extends RuntimeException {

    public AvatarStorageException(
            String message,
            Throwable cause) {
        super(message, cause);
    }
}