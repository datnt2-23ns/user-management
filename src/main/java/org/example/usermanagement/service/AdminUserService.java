package org.example.usermanagement.service;

import org.example.usermanagement.dto.response.AdminUserDetailResponse;
import org.example.usermanagement.dto.response.AdminUserListItemResponse;
import org.example.usermanagement.dto.response.PageResponse;

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
}