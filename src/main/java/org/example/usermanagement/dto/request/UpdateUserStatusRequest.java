package org.example.usermanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import org.example.usermanagement.enums.UserStatus;

public record UpdateUserStatusRequest(

        @NotNull(message = "Trạng thái tài khoản không được để trống") UserStatus status

) {
    @AssertTrue(message = "Trạng thái chỉ nhận ACTIVE hoặc LOCKED")
    public boolean isSupportedStatus() {
        return status == null
                || status == UserStatus.ACTIVE
                || status == UserStatus.LOCKED;
    }
}