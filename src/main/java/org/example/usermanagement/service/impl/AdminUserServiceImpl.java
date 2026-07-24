package org.example.usermanagement.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.usermanagement.dto.request.UpdateUserStatusRequest;
import org.example.usermanagement.dto.response.AdminUserListItemResponse;
import org.example.usermanagement.dto.response.PageResponse;
import org.example.usermanagement.dto.response.AdminUserDetailResponse;
import org.example.usermanagement.dto.response.AdminUserStatusResponse;
import org.example.usermanagement.exception.AdminUserNotFoundException;
import org.example.usermanagement.exception.CurrentUserNotFoundException;
import org.example.usermanagement.exception.SelfLockNotAllowedException;
import org.example.usermanagement.entity.User;
import org.example.usermanagement.exception.InvalidUserListQueryException;
import org.example.usermanagement.repository.UserRepository;
import org.example.usermanagement.service.AdminUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;
import org.example.usermanagement.enums.Role;
import org.example.usermanagement.enums.UserStatus;
import org.example.usermanagement.dto.request.UpdateUserRoleRequest;
import org.example.usermanagement.dto.response.AdminUserRoleResponse;
import org.example.usermanagement.exception.LastActiveAdminException;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.time.LocalDateTime;

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
                        String keyword,
                        String role,
                        String status,
                        String sortBy,
                        String direction) {
                validatePagination(page, size);

                String normalizedKeyword = normalizeKeyword(keyword);

                String validatedSortField = validateSortField(sortBy);

                Sort.Direction sortDirection = parseSortDirection(direction);

                Role parsedRole = parseRole(role);

                UserStatus parsedStatus = parseStatus(status);

                PageRequest pageRequest = PageRequest.of(
                                page,
                                size,
                                Sort.by(
                                                sortDirection,
                                                validatedSortField));

                Specification<User> specification = buildSpecification(
                                normalizedKeyword,
                                parsedRole,
                                parsedStatus);

                Page<AdminUserListItemResponse> userPage = userRepository
                                .findAll(
                                                specification,
                                                pageRequest)
                                .map(this::mapToResponse);

                return PageResponse.from(userPage);
        }

        @Override
        @Transactional(readOnly = true)
        public AdminUserDetailResponse getUserById(Long userId) {
                User user = userRepository
                                .findById(userId)
                                .orElseThrow(
                                                () -> new AdminUserNotFoundException(userId));

                return mapToDetailResponse(user);
        }

        @Override
        @Transactional
        public AdminUserStatusResponse updateUserStatus(
                        Long userId,
                        String adminEmail,
                        UpdateUserStatusRequest request) {
                User currentAdmin = userRepository
                                .findByEmailIgnoreCase(adminEmail)
                                .orElseThrow(
                                                () -> new CurrentUserNotFoundException(
                                                                "Không tìm thấy quản trị viên đang đăng nhập"));

                User targetUser = userRepository
                                .findById(userId)
                                .orElseThrow(
                                                () -> new AdminUserNotFoundException(userId));

                UserStatus requestedStatus = request.status();

                if (currentAdmin.getId().equals(targetUser.getId())
                                && requestedStatus == UserStatus.LOCKED) {
                        throw new SelfLockNotAllowedException();
                }

                if (targetUser.getStatus() == requestedStatus) {
                        return mapToStatusResponse(targetUser);
                }

                targetUser.setStatus(requestedStatus);
                targetUser.setUpdatedBy(currentAdmin.getId());

                if (requestedStatus == UserStatus.LOCKED) {
                        targetUser.setLockedBy(currentAdmin.getId());
                        targetUser.setLockedAt(LocalDateTime.now());
                } else {
                        targetUser.setLockedBy(null);
                        targetUser.setLockedAt(null);
                }

                User savedUser = userRepository.saveAndFlush(targetUser);

                return mapToStatusResponse(savedUser);
        }

        @Override
        @Transactional
        public AdminUserRoleResponse updateUserRole(
                        Long userId,
                        String adminEmail,
                        UpdateUserRoleRequest request) {
                User currentAdmin = userRepository
                                .findByEmailIgnoreCase(adminEmail)
                                .orElseThrow(
                                                () -> new CurrentUserNotFoundException(
                                                                "Không tìm thấy quản trị viên đang đăng nhập"));

                User targetUser = userRepository
                                .findById(userId)
                                .filter(
                                                user -> user.getStatus() != UserStatus.DELETED)
                                .orElseThrow(
                                                () -> new AdminUserNotFoundException(
                                                                userId));

                Role requestedRole = request.role();

                if (targetUser.getRole() == requestedRole) {
                        return mapToRoleResponse(
                                        targetUser);
                }

                boolean demotingActiveAdmin = targetUser.getRole() == Role.ADMIN
                                && requestedRole == Role.USER
                                && targetUser.getStatus() == UserStatus.ACTIVE;

                if (demotingActiveAdmin) {
                        long activeAdminCount = userRepository.countByRoleAndStatus(
                                        Role.ADMIN,
                                        UserStatus.ACTIVE);

                        if (activeAdminCount <= 1) {
                                throw new LastActiveAdminException();
                        }
                }

                targetUser.setRole(
                                requestedRole);

                targetUser.setUpdatedBy(
                                currentAdmin.getId());

                User savedUser = userRepository.saveAndFlush(
                                targetUser);

                return mapToRoleResponse(
                                savedUser);
        }

        private AdminUserDetailResponse mapToDetailResponse(User user) {
                return new AdminUserDetailResponse(
                                user.getId(),
                                user.getFirstName(),
                                user.getLastName(),
                                user.getFullName(),
                                user.getEmail(),
                                user.getDateOfBirth(),
                                user.getGender(),
                                user.getAddress(),
                                user.getPhone(),
                                user.getAvatarUrl(),
                                user.getRole(),
                                user.getStatus(),
                                user.getUpdatedBy(),
                                user.getLockedBy(),
                                user.getLockedAt(),
                                user.getDeletedBy(),
                                user.getDeletedAt(),
                                user.getCreatedAt(),
                                user.getUpdatedAt());
        }

        private String normalizeKeyword(
                        String keyword) {
                if (keyword == null) {
                        return "";
                }

                String normalized = keyword
                                .trim()
                                .replaceAll("\\s+", " ");

                if (normalized.length() > 100) {
                        throw new InvalidUserListQueryException(
                                        "Từ khóa tìm kiếm không được vượt quá 100 ký tự");
                }

                return normalized;
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

        private Role parseRole(
                        String role) {
                if (role == null
                                || role.isBlank()) {
                        return null;
                }

                try {
                        return Role.valueOf(
                                        role.trim()
                                                        .toUpperCase(
                                                                        Locale.ROOT));
                } catch (IllegalArgumentException exception) {
                        throw new InvalidUserListQueryException(
                                        "Vai trò chỉ nhận USER hoặc ADMIN");
                }
        }

        private UserStatus parseStatus(
                        String status) {
                if (status == null
                                || status.isBlank()) {
                        return null;
                }

                try {
                        return UserStatus.valueOf(
                                        status.trim()
                                                        .toUpperCase(
                                                                        Locale.ROOT));
                } catch (IllegalArgumentException exception) {
                        throw new InvalidUserListQueryException(
                                        "Trạng thái chỉ nhận ACTIVE hoặc LOCKED");
                }
        }

        private Specification<User> buildSpecification(
                        String keyword,
                        Role role,
                        UserStatus status) {
                return (root, query, criteriaBuilder) -> {
                        List<Predicate> predicates = new ArrayList<>();

                        if (keyword != null
                                        && !keyword.isBlank()) {
                                String pattern = "%"
                                                + keyword.toLowerCase(Locale.ROOT)
                                                + "%";

                                Expression<String> fullName = criteriaBuilder.lower(
                                                criteriaBuilder.concat(
                                                                criteriaBuilder.concat(
                                                                                root.get("lastName"),
                                                                                " "),
                                                                root.get("firstName")));

                                Predicate keywordPredicate = criteriaBuilder.or(
                                                criteriaBuilder.like(
                                                                criteriaBuilder.lower(
                                                                                root.get("firstName")),
                                                                pattern),
                                                criteriaBuilder.like(
                                                                criteriaBuilder.lower(
                                                                                root.get("lastName")),
                                                                pattern),
                                                criteriaBuilder.like(
                                                                fullName,
                                                                pattern),
                                                criteriaBuilder.like(
                                                                criteriaBuilder.lower(
                                                                                root.get("email")),
                                                                pattern),
                                                criteriaBuilder.like(
                                                                root.get("phone"),
                                                                pattern));

                                predicates.add(keywordPredicate);
                        }

                        if (role != null) {
                                predicates.add(
                                                criteriaBuilder.equal(
                                                                root.get("role"),
                                                                role));
                        }

                        if (status != null) {
                                predicates.add(
                                                criteriaBuilder.equal(
                                                                root.get("status"),
                                                                status));
                        }

                        return criteriaBuilder.and(
                                        predicates.toArray(
                                                        new Predicate[0]));
                };
        }

        private AdminUserStatusResponse mapToStatusResponse(User user) {
                return new AdminUserStatusResponse(
                                user.getId(),
                                user.getFullName(),
                                user.getEmail(),
                                user.getRole(),
                                user.getStatus(),
                                user.getUpdatedBy(),
                                user.getLockedBy(),
                                user.getLockedAt(),
                                user.getUpdatedAt());
        }

        private AdminUserRoleResponse mapToRoleResponse(
                        User user) {
                return new AdminUserRoleResponse(
                                user.getId(),
                                user.getFullName(),
                                user.getEmail(),
                                user.getRole(),
                                user.getStatus(),
                                user.getUpdatedBy(),
                                user.getUpdatedAt());
        }
}