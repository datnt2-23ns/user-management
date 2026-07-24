package org.example.usermanagement.dto.response;

import org.example.usermanagement.enums.Role;
import org.example.usermanagement.enums.UserStatus;

import java.time.LocalDateTime;

public record AdminUserRoleResponse(
                Long id,
                String fullName,
                String email,
                Role role,
                UserStatus status,
                Long updatedBy,
                LocalDateTime updatedAt) {
}