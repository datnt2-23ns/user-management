package org.example.usermanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.usermanagement.enums.Gender;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin cập nhật hồ sơ cá nhân")
public class UpdateProfileRequest {

        @Schema(description = "Tên của người dùng", example = "An", maxLength = 100)
        @NotBlank(message = "Tên không được để trống")
        @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
        private String firstName;

        @Schema(description = "Họ và tên đệm của người dùng", example = "Nguyễn Văn", maxLength = 150)
        @NotBlank(message = "Họ và tên đệm không được để trống")
        @Size(max = 150, message = "Họ và tên đệm không được vượt quá 150 ký tự")
        private String lastName;

        @Schema(description = "Ngày sinh", example = "2002-05-10", type = "string", format = "date")
        @NotNull(message = "Ngày sinh không được để trống")
        @PastOrPresent(message = "Ngày sinh không được lớn hơn ngày hiện tại")
        private LocalDate dateOfBirth;

        @Schema(description = "Giới tính", example = "MALE", allowableValues = { "MALE", "FEMALE", "OTHER" })
        @NotNull(message = "Giới tính không được để trống")
        private Gender gender;

        @Schema(description = "Địa chỉ liên hệ", example = "TP. Đà Nẵng", maxLength = 255)
        @NotBlank(message = "Địa chỉ không được để trống")
        @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
        private String address;

        @Schema(description = "Số điện thoại gồm đúng 10 chữ số", example = "0987654321", pattern = "^\\d{10}$")
        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^\\d{10}$", message = "Số điện thoại phải gồm đúng 10 chữ số")
        private String phone;
}