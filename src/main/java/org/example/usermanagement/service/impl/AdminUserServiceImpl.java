package org.example.usermanagement.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.usermanagement.dto.response.AdminUserListItemResponse;
import org.example.usermanagement.dto.response.PageResponse;
import org.example.usermanagement.entity.User;
import org.example.usermanagement.exception.InvalidUserListQueryException;
import org.example.usermanagement.repository.UserRepository;
import org.example.usermanagement.service.AdminUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl
                implements AdminUserService {

        private static final int MAX_PAGE_SIZE = 100;

        private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
                        "id",
                        "firstName",
                        "lastName",
                        "email",
                        "role",
                        "status",
                        "createdAt",
                        "updatedAt");

        private final UserRepository userRepository;

        @Override
        @Transactional(readOnly = true)
        public PageResponse<AdminUserListItemResponse> getUsers(
                        int page,
                        int size,
                        String sortBy,
                        String direction) {
                validatePagination(
                                page,
                                size);

                String validatedSortField = validateSortField(
                                sortBy);

                Sort.Direction sortDirection = parseSortDirection(
                                direction);

                PageRequest pageRequest = PageRequest.of(
                                page,
                                size,
                                Sort.by(
                                                sortDirection,
                                                validatedSortField));

                Page<AdminUserListItemResponse> userPage = userRepository
                                .findAll(pageRequest)
                                .map(this::mapToResponse);

                return PageResponse.from(
                                userPage);
        }

        private void validatePagination(
                        int page,
                        int size) {
                if (page < 0) {
                        throw new InvalidUserListQueryException(
                                        "Số trang không được nhỏ hơn 0");
                }

                if (size < 1
                                || size > MAX_PAGE_SIZE) {
                        throw new InvalidUserListQueryException(
                                        "Kích thước trang phải từ 1 đến 100");
                }
        }

        private String validateSortField(
                        String sortBy) {
                if (sortBy == null
                                || !ALLOWED_SORT_FIELDS.contains(
                                                sortBy)) {
                        throw new InvalidUserListQueryException(
                                        "Trường sắp xếp không hợp lệ");
                }

                return sortBy;
        }

        private Sort.Direction parseSortDirection(
                        String direction) {
                if ("asc".equalsIgnoreCase(
                                direction)) {
                        return Sort.Direction.ASC;
                }

                if ("desc".equalsIgnoreCase(
                                direction)) {
                        return Sort.Direction.DESC;
                }

                throw new InvalidUserListQueryException(
                                "Chiều sắp xếp chỉ nhận asc hoặc desc");
        }

        private AdminUserListItemResponse mapToResponse(
                        User user) {
                return new AdminUserListItemResponse(
                                user.getId(),
                                user.getFirstName(),
                                user.getLastName(),
                                user.getFullName(),
                                user.getEmail(),
                                user.getAvatarUrl(),
                                user.getRole(),
                                user.getStatus(),
                                user.getCreatedAt(),
                                user.getUpdatedAt());
        }
}