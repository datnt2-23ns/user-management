package org.example.usermanagement.service.impl;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.usermanagement.dto.request.UpdateUserRoleRequest;
import org.example.usermanagement.dto.request.UpdateUserStatusRequest;
import org.example.usermanagement.dto.response.AdminUserDetailResponse;
import org.example.usermanagement.dto.response.AdminUserListItemResponse;
import org.example.usermanagement.dto.response.AdminUserRoleResponse;
import org.example.usermanagement.dto.response.AdminUserStatusResponse;
import org.example.usermanagement.dto.response.PageResponse;
import org.example.usermanagement.entity.User;
import org.example.usermanagement.enums.Role;
import org.example.usermanagement.enums.UserStatus;
import org.example.usermanagement.exception.AdminUserNotFoundException;
import org.example.usermanagement.exception.CurrentUserNotFoundException;
import org.example.usermanagement.exception.InvalidUserListQueryException;
import org.example.usermanagement.exception.LastActiveAdminException;
import org.example.usermanagement.exception.SelfDeleteNotAllowedException;
import org.example.usermanagement.exception.SelfLockNotAllowedException;
import org.example.usermanagement.repository.UserRepository;
import org.example.usermanagement.service.AdminUserService;
import org.example.usermanagement.service.AvatarStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

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
        private final AvatarStorageService avatarStorageService;

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
                                Sort.by(sortDirection, validatedSortField));

                Specification<User> specification = buildSpecification(
                                normalizedKeyword,
                                parsedRole,
                                parsedStatus);

                Page<AdminUserListItemResponse> userPage = userRepository
                                .findAll(specification, pageRequest)
                                .map(this::mapToListItemResponse);

                return PageResponse.from(userPage);
        }

        @Override
        @Transactional(readOnly = true)
        public AdminUserDetailResponse getUserById(Long userId) {
                User user = findAvailableUserById(userId);

                return mapToDetailResponse(user);
        }

        @Override
        @Transactional
        public AdminUserStatusResponse updateUserStatus(
                        Long userId,
                        String adminEmail,
                        UpdateUserStatusRequest request) {

                User currentAdmin = findCurrentAdmin(adminEmail);
                User targetUser = findAvailableUserById(userId);

                UserStatus requestedStatus = request.status();

                if (requestedStatus != UserStatus.ACTIVE
                                && requestedStatus != UserStatus.LOCKED) {
                        throw new InvalidUserListQueryException(
                                        "Trạng thái chỉ nhận ACTIVE hoặc LOCKED");
                }

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
        public void deleteUser(
                        Long userId,
                        String adminEmail) {

                User currentAdmin = findCurrentAdmin(adminEmail);
                User targetUser = findAvailableUserById(userId);

                if (currentAdmin.getId().equals(targetUser.getId())) {
                        throw new SelfDeleteNotAllowedException();
                }

                String oldAvatarUrl = targetUser.getAvatarUrl();

                targetUser.setStatus(UserStatus.DELETED);
                targetUser.setDeletedBy(currentAdmin.getId());
                targetUser.setDeletedAt(LocalDateTime.now());
                targetUser.setUpdatedBy(currentAdmin.getId());

                targetUser.setLockedBy(null);
                targetUser.setLockedAt(null);
                targetUser.setAvatarUrl(null);

                userRepository.saveAndFlush(targetUser);

                deleteAvatarAfterTransactionCommit(oldAvatarUrl);
        }

        @Override
        @Transactional
        public AdminUserRoleResponse updateUserRole(
                        Long userId,
                        String adminEmail,
                        UpdateUserRoleRequest request) {

                User currentAdmin = findCurrentAdmin(adminEmail);
                User targetUser = findAvailableUserById(userId);

                Role requestedRole = request.role();

                if (requestedRole != Role.USER
                                && requestedRole != Role.ADMIN) {
                        throw new InvalidUserListQueryException(
                                        "Vai trò chỉ nhận USER hoặc ADMIN");
                }

                if (targetUser.getRole() == requestedRole) {
                        return mapToRoleResponse(targetUser);
                }

                boolean isDemotingActiveAdmin = targetUser.getRole() == Role.ADMIN
                                && requestedRole == Role.USER
                                && targetUser.getStatus() == UserStatus.ACTIVE;

                if (isDemotingActiveAdmin) {
                        long activeAdminCount = userRepository.countByRoleAndStatus(
                                        Role.ADMIN,
                                        UserStatus.ACTIVE);

                        if (activeAdminCount <= 1) {
                                throw new LastActiveAdminException();
                        }
                }

                targetUser.setRole(requestedRole);
                targetUser.setUpdatedBy(currentAdmin.getId());

                User savedUser = userRepository.saveAndFlush(targetUser);

                return mapToRoleResponse(savedUser);
        }

        private User findCurrentAdmin(String adminEmail) {
                if (adminEmail == null || adminEmail.isBlank()) {
                        throw new CurrentUserNotFoundException(
                                        "Không xác định được quản trị viên đang đăng nhập");
                }

                return userRepository
                                .findByEmailIgnoreCase(adminEmail.trim())
                                .filter(user -> user.getStatus() != UserStatus.DELETED)
                                .orElseThrow(
                                                () -> new CurrentUserNotFoundException(
                                                                "Không tìm thấy quản trị viên đang đăng nhập"));
        }

        private User findAvailableUserById(Long userId) {
                return userRepository
                                .findById(userId)
                                .filter(user -> user.getStatus() != UserStatus.DELETED)
                                .orElseThrow(
                                                () -> new AdminUserNotFoundException(userId));
        }

        private AdminUserListItemResponse mapToListItemResponse(User user) {
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
                                user.getUpdatedAt(),
                                resolveUserFullName(user.getUpdatedBy()),
                                resolveUserFullName(user.getLockedBy()),
                                resolveUserFullName(user.getDeletedBy()));
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
                                user.getUpdatedAt(),
                                resolveUserFullName(user.getUpdatedBy()),
                                resolveUserFullName(user.getLockedBy()));
        }

        private AdminUserRoleResponse mapToRoleResponse(User user) {
                return new AdminUserRoleResponse(
                                user.getId(),
                                user.getFullName(),
                                user.getEmail(),
                                user.getRole(),
                                user.getStatus(),
                                user.getUpdatedBy(),
                                user.getUpdatedAt(),
                                resolveUserFullName(user.getUpdatedBy()));
        }

        private String normalizeKeyword(String keyword) {
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

        private void validatePagination(int page, int size) {
                if (page < 0) {
                        throw new InvalidUserListQueryException(
                                        "Số trang không được nhỏ hơn 0");
                }

                if (size < 1 || size > MAX_PAGE_SIZE) {
                        throw new InvalidUserListQueryException(
                                        "Kích thước trang phải từ 1 đến 100");
                }
        }

        private String validateSortField(String sortBy) {
                if (sortBy == null
                                || !ALLOWED_SORT_FIELDS.contains(sortBy)) {
                        throw new InvalidUserListQueryException(
                                        "Trường sắp xếp không hợp lệ");
                }

                return sortBy;
        }

        private Sort.Direction parseSortDirection(String direction) {
                if ("asc".equalsIgnoreCase(direction)) {
                        return Sort.Direction.ASC;
                }

                if ("desc".equalsIgnoreCase(direction)) {
                        return Sort.Direction.DESC;
                }

                throw new InvalidUserListQueryException(
                                "Chiều sắp xếp chỉ nhận asc hoặc desc");
        }

        private Role parseRole(String role) {
                if (role == null || role.isBlank()) {
                        return null;
                }

                String normalizedRole = role
                                .trim()
                                .toUpperCase(Locale.ROOT);

                if ("USER".equals(normalizedRole)) {
                        return Role.USER;
                }

                if ("ADMIN".equals(normalizedRole)) {
                        return Role.ADMIN;
                }

                throw new InvalidUserListQueryException(
                                "Vai trò chỉ nhận USER hoặc ADMIN");
        }

        private UserStatus parseStatus(String status) {
                if (status == null || status.isBlank()) {
                        return null;
                }

                String normalizedStatus = status
                                .trim()
                                .toUpperCase(Locale.ROOT);

                if ("ACTIVE".equals(normalizedStatus)) {
                        return UserStatus.ACTIVE;
                }

                if ("LOCKED".equals(normalizedStatus)) {
                        return UserStatus.LOCKED;
                }

                throw new InvalidUserListQueryException(
                                "Trạng thái chỉ nhận ACTIVE hoặc LOCKED");
        }

        private Specification<User> buildSpecification(
                        String keyword,
                        Role role,
                        UserStatus status) {

                return (root, query, criteriaBuilder) -> {
                        List<Predicate> predicates = new ArrayList<>();

                        predicates.add(
                                        criteriaBuilder.notEqual(
                                                        root.get("status"),
                                                        UserStatus.DELETED));

                        if (keyword != null && !keyword.isBlank()) {
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
                                        predicates.toArray(new Predicate[0]));
                };
        }

        private void deleteAvatarAfterTransactionCommit(String avatarUrl) {
                if (avatarUrl == null || avatarUrl.isBlank()) {
                        return;
                }

                if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                        deleteAvatarSafely(avatarUrl);
                        return;
                }

                TransactionSynchronizationManager.registerSynchronization(
                                new TransactionSynchronization() {
                                        @Override
                                        public void afterCommit() {
                                                deleteAvatarSafely(avatarUrl);
                                        }
                                });
        }

        private void deleteAvatarSafely(String avatarUrl) {
                try {
                        avatarStorageService.deleteByUrl(avatarUrl);
                } catch (RuntimeException exception) {
                        log.warn(
                                        "Không thể xóa avatar của tài khoản đã xóa: {}",
                                        avatarUrl,
                                        exception);
                }
        }

        private String resolveUserFullName(Long userId) {
                if (userId == null) {
                        return null;
                }

                return userRepository.findById(userId)
                                .map(User::getFullName)
                                .orElse("Tài khoản không còn tồn tại");
        }
}