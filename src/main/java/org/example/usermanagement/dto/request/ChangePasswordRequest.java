package org.example.usermanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Thông tin đổi mật khẩu")
public record ChangePasswordRequest(

                @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @Schema(description = "Mật khẩu hiện tại", example = "Current@123", maxLength = 64, accessMode = Schema.AccessMode.WRITE_ONLY) @NotBlank(message = "Mật khẩu hiện tại không được để trống") @Size(max = 64, message = "Mật khẩu hiện tại không được vượt quá 64 ký tự") String currentPassword,

                @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @Schema(description = "Mật khẩu mới có từ 8 đến 64 ký tự", example = "NewPassword@123", minLength = 8, maxLength = 64, accessMode = Schema.AccessMode.WRITE_ONLY) @NotBlank(message = "Mật khẩu mới không được để trống") @Size(min = 8, max = 64, message = "Mật khẩu mới phải có từ 8 đến 64 ký tự") String newPassword,

                @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @Schema(description = "Nhập lại mật khẩu mới", example = "NewPassword@123", minLength = 8, maxLength = 64, accessMode = Schema.AccessMode.WRITE_ONLY) @NotBlank(message = "Xác nhận mật khẩu không được để trống") @Size(min = 8, max = 64, message = "Xác nhận mật khẩu phải có từ 8 đến 64 ký tự") String confirmPassword

) {
}