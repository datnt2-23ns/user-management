package org.example.usermanagement.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.usermanagement.dto.request.UpdateProfileRequest;
import org.example.usermanagement.dto.response.UserProfileResponse;
import org.example.usermanagement.entity.User;
import org.example.usermanagement.exception.UserNotFoundException;
import org.example.usermanagement.repository.UserRepository;
import org.example.usermanagement.service.AvatarStorageService;
import org.example.usermanagement.service.UserService;
import org.example.usermanagement.dto.request.ChangePasswordRequest;
import org.example.usermanagement.exception.InvalidPasswordChangeException;
import org.example.usermanagement.enums.UserStatus;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

        private final UserRepository userRepository;
        private final AvatarStorageService avatarStorageService;
        private final PasswordEncoder passwordEncoder;

        @Override
        @Transactional(readOnly = true)
        public UserProfileResponse getCurrentUserProfile(
                        String email) {
                String normalizedEmail = normalizeEmail(email);

                User user = userRepository
                                .findByEmailIgnoreCase(normalizedEmail)
                                .orElseThrow(() -> new UserNotFoundException(
                                                "Không tìm thấy tài khoản đang đăng nhập"));

                return mapToProfileResponse(user);
        }

        @Override
        @Transactional
        public UserProfileResponse updateCurrentUserProfile(
                        String email,
                        UpdateProfileRequest request) {
                String normalizedEmail = normalizeEmail(email);

                User user = userRepository
                                .findByEmailIgnoreCase(normalizedEmail)
                                .orElseThrow(() -> new UserNotFoundException(
                                                "Không tìm thấy tài khoản đang đăng nhập"));

                user.setFirstName(
                                normalizeText(request.getFirstName()));

                user.setLastName(
                                normalizeText(request.getLastName()));

                user.setDateOfBirth(
                                request.getDateOfBirth());

                user.setGender(
                                request.getGender());

                user.setAddress(
                                normalizeText(request.getAddress()));

                user.setPhone(
                                normalizePhone(request.getPhone()));

                user.setUpdatedBy(user.getId());

                User updatedUser = userRepository.saveAndFlush(user);

                return mapToProfileResponse(updatedUser);
        }

        @Override
        @Transactional
        public UserProfileResponse updateCurrentUserAvatar(
                        String email,
                        MultipartFile file) {
                String normalizedEmail = normalizeEmail(email);

                User user = userRepository
                                .findByEmailIgnoreCase(normalizedEmail)
                                .orElseThrow(() -> new UserNotFoundException(
                                                "Không tìm thấy tài khoản đang đăng nhập"));

                String oldAvatarUrl = user.getAvatarUrl();

                String newAvatarUrl = avatarStorageService.store(file);

                try {
                        user.setAvatarUrl(newAvatarUrl);
                        user.setUpdatedBy(user.getId());

                        User updatedUser = userRepository.saveAndFlush(user);

                        registerAvatarCleanup(
                                        oldAvatarUrl,
                                        newAvatarUrl);

                        return mapToProfileResponse(updatedUser);
                } catch (RuntimeException exception) {
                        avatarStorageService.deleteByUrl(
                                        newAvatarUrl);

                        throw exception;
                }
        }

        @Override
        @Transactional
        public void changeCurrentUserPassword(
                        String email,
                        ChangePasswordRequest request) {
                String normalizedEmail = normalizeEmail(email);

                User user = userRepository
                                .findByEmailIgnoreCase(normalizedEmail)
                                .orElseThrow(() -> new UserNotFoundException(
                                                "Không tìm thấy tài khoản đang đăng nhập"));

                if (user.getStatus() == UserStatus.LOCKED) {
                        throw new LockedException(
                                        "Tài khoản đã bị khóa");
                }

                if (!passwordEncoder.matches(
                                request.currentPassword(),
                                user.getPassword())) {
                        throw new InvalidPasswordChangeException(
                                        "Mật khẩu hiện tại không chính xác");
                }

                if (!request.newPassword().equals(
                                request.confirmPassword())) {
                        throw new InvalidPasswordChangeException(
                                        "Mật khẩu xác nhận không khớp");
                }

                if (passwordEncoder.matches(
                                request.newPassword(),
                                user.getPassword())) {
                        throw new InvalidPasswordChangeException(
                                        "Mật khẩu mới phải khác mật khẩu hiện tại");
                }

                user.setPassword(
                                passwordEncoder.encode(
                                                request.newPassword()));

                user.setUpdatedBy(
                                user.getId());

                userRepository.saveAndFlush(
                                user);
        }

        private void registerAvatarCleanup(
                        String oldAvatarUrl,
                        String newAvatarUrl) {
                if (!TransactionSynchronizationManager
                                .isSynchronizationActive()) {

                        avatarStorageService.deleteByUrl(
                                        oldAvatarUrl);

                        return;
                }

                TransactionSynchronizationManager
                                .registerSynchronization(
                                                new TransactionSynchronization() {

                                                        @Override
                                                        public void afterCommit() {
                                                                avatarStorageService
                                                                                .deleteByUrl(
                                                                                                oldAvatarUrl);
                                                        }

                                                        @Override
                                                        public void afterCompletion(
                                                                        int status) {
                                                                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                                                                        avatarStorageService
                                                                                        .deleteByUrl(
                                                                                                        newAvatarUrl);
                                                                }
                                                        }
                                                });
        }

        private UserProfileResponse mapToProfileResponse(
                        User user) {
                return UserProfileResponse.builder()
                                .id(user.getId())
                                .firstName(user.getFirstName())
                                .lastName(user.getLastName())
                                .fullName(user.getFullName())
                                .email(user.getEmail())
                                .dateOfBirth(user.getDateOfBirth())
                                .gender(user.getGender())
                                .address(user.getAddress())
                                .phone(user.getPhone())
                                .avatarUrl(user.getAvatarUrl())
                                .role(user.getRole())
                                .status(user.getStatus())
                                .createdAt(user.getCreatedAt())
                                .updatedAt(user.getUpdatedAt())
                                .build();
        }

        private String normalizeEmail(
                        String email) {
                if (email == null) {
                        return "";
                }

                return email
                                .trim()
                                .toLowerCase(Locale.ROOT);
        }

        private String normalizeText(
                        String value) {
                if (value == null) {
                        return "";
                }

                return value
                                .trim()
                                .replaceAll("\\s+", " ");
        }

        private String normalizePhone(
                        String phone) {
                if (phone == null) {
                        return "";
                }

                return phone.trim();
        }
}