package org.example.usermanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import org.example.usermanagement.enums.UserStatus;

public record UpdateUserStatusRequest(

                @NotNull(message = "Trạng thái tài khoản không được để trống") UserStatus status

) {
}