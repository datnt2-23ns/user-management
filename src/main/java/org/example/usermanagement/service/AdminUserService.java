package org.example.usermanagement.service;

import org.example.usermanagement.dto.response.AdminUserDetailResponse;
import org.example.usermanagement.dto.response.AdminUserListItemResponse;
import org.example.usermanagement.dto.response.PageResponse;
import org.example.usermanagement.dto.response.AdminUserStatusResponse;
import org.example.usermanagement.dto.request.UpdateUserStatusRequest;
import org.example.usermanagement.dto.request.UpdateUserRoleRequest;
import org.example.usermanagement.dto.response.AdminUserRoleResponse;

public interface AdminUserService {

        PageResponse<AdminUserListItemResponse> getUsers(
                        int page,
                        int size,
                        String keyword,
                        String role,
                        String status,
                        String sortBy,
                        String direction);

        AdminUserDetailResponse getUserById(Long userId);

        AdminUserStatusResponse updateUserStatus(
                        Long userId,
                        String adminEmail,
                        UpdateUserStatusRequest request);

        void deleteUser(
                        Long userId,
                        String adminEmail);

        AdminUserRoleResponse updateUserRole(
                        Long userId,
                        String adminEmail,
                        UpdateUserRoleRequest request);
}