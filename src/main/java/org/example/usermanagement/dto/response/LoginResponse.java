package org.example.usermanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.usermanagement.enums.Gender;
import org.example.usermanagement.enums.Role;
import org.example.usermanagement.enums.UserStatus;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private long expiresIn;

    private UserInfo user;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserInfo {

        private Long id;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private Gender gender;
        private String avatarUrl;
        private Role role;
        private UserStatus status;
    }
}