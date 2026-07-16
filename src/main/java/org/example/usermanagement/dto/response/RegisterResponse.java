package org.example.usermanagement.dto.response;

import org.example.usermanagement.enums.Gender;
import org.example.usermanagement.enums.Role;
import org.example.usermanagement.enums.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class RegisterResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private String phone;
    private String avatarUrl;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;
}
