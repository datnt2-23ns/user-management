package org.example.usermanagement.dto.request;

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
public class LoginRequest {

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 320, message = "Email không được vượt quá 320 ký tự")
        private String email;

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(max = 64, message = "Mật khẩu không được vượt quá 64 ký tự")
        private String password;
}