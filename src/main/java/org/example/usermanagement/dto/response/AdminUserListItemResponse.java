package org.example.usermanagement.dto.response;

import org.example.usermanagement.enums.Role;
import org.example.usermanagement.enums.UserStatus;

import java.time.LocalDateTime;

public record AdminUserListItemResponse(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String avatarUrl,
        Role role,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}