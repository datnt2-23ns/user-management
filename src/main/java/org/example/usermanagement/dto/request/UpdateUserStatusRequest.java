package org.example.usermanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import org.example.usermanagement.enums.UserStatus;

public record UpdateUserStatusRequest(

                @Schema(description = "Trạng thái mới", example = "LOCKED", allowableValues = {
                                "ACTIVE", "LOCKED" })

                @NotNull(message = "Trạng thái tài khoản không được để trống") UserStatus status

        ) {
        @AssertTrue(message = "Trạng thái chỉ nhận ACTIVE hoặc LOCKED")
        public boolean isSupportedStatus() {
                return status == null
                                || status == UserStatus.ACTIVE
                                || status == UserStatus.LOCKED;
        }
}