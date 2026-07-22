package org.example.usermanagement.exception;

public class AdminUserNotFoundException extends RuntimeException {

    public AdminUserNotFoundException(Long userId) {
        super("Không tìm thấy tài khoản có ID: " + userId);
    }
}