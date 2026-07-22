package org.example.usermanagement.exception;

public class SelfDeleteNotAllowedException
        extends RuntimeException {

    public SelfDeleteNotAllowedException() {
        super(
                "Quản trị viên không thể tự xóa tài khoản của mình");
    }
}