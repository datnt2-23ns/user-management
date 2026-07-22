package org.example.usermanagement.exception;

public class SelfLockNotAllowedException extends RuntimeException {

    public SelfLockNotAllowedException() {
        super("Quản trị viên không thể tự khóa tài khoản của mình");
    }
}