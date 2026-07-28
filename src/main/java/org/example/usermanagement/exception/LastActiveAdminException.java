package org.example.usermanagement.exception;

public class LastActiveAdminException extends RuntimeException {

    public LastActiveAdminException() {
        super(
                "Không thể hạ vai trò của Admin đang hoạt động cuối cùng trong hệ thống"
        );
    }
}