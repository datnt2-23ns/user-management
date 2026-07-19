package org.example.usermanagement.dto.request;

import org.example.usermanagement.enums.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

        @NotBlank(message = "Tên không được để trống")
        @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
        private String firstName;

        @NotBlank(message = "Họ và tên đệm không được để trống")
        @Size(max = 150, message = "Họ và tên đệm không được vượt quá 150 ký tự")
        private String lastName;

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 320, message = "Email không được vượt quá 320 ký tự")
        private String email;

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, max = 64, message = "Mật khẩu phải có từ 8 đến 64 ký tự")
        private String password;

        @NotNull(message = "Ngày sinh không được để trống")
        @PastOrPresent(message = "Ngày sinh không được lớn hơn ngày hiện tại")
        private LocalDate dateOfBirth;

        @NotNull(message = "Giới tính không được để trống")
        private Gender gender;

        @NotBlank(message = "Địa chỉ không được để trống")
        @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
        private String address;

        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^\\d{10}$", message = "Số điện thoại phải gồm đúng 10 chữ số")
        private String phone;
}