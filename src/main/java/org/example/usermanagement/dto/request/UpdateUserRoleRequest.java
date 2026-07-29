package org.example.usermanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import org.example.usermanagement.enums.Role;

public record UpdateUserRoleRequest(

                @Schema(description = "Vai trò mới của tài khoản", example = "ADMIN", allowableValues = {
                                "USER", "ADMIN" })

                @NotNull(message = "Vai trò tài khoản không được để trống") Role role

        ) {

        @AssertTrue(message = "Vai trò chỉ nhận USER hoặc ADMIN")
        public boolean isSupportedRole() {
                return role == null
                                || role == Role.USER
                                || role == Role.ADMIN;
        }
}