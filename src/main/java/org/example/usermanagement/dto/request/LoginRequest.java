package org.example.usermanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin đăng nhập")
public class LoginRequest {

        @Schema(description = "Email đăng nhập", example = "admin@example.com", maxLength = 320)
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 320, message = "Email không được vượt quá 320 ký tự")
        private String email;

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @Schema(description = "Mật khẩu đăng nhập", example = "Example@123", maxLength = 64, accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(max = 64, message = "Mật khẩu không được vượt quá 64 ký tự")
        private String password;
}