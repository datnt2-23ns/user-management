package org.example.usermanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(

                @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @NotBlank(message = "Mật khẩu hiện tại không được để trống") String currentPassword,

                @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @NotBlank(message = "Mật khẩu mới không được để trống") @Size(min = 8, max = 100, message = "Mật khẩu mới phải có từ 8 đến 100 ký tự") String newPassword,

                @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @NotBlank(message = "Xác nhận mật khẩu không được để trống") @Size(max = 100, message = "Xác nhận mật khẩu không được vượt quá 100 ký tự") String confirmPassword

) {
}