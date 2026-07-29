package org.example.usermanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.usermanagement.dto.request.UpdateProfileRequest;
import org.example.usermanagement.dto.response.UserProfileResponse;
import org.example.usermanagement.exception.CurrentUserNotFoundException;
import org.example.usermanagement.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.example.usermanagement.dto.request.ChangePasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Hồ sơ người dùng", description = "Xem và cập nhật thông tin của tài khoản đang đăng nhập")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

        private final UserService userService;

        @Operation(summary = "Xem hồ sơ cá nhân")

        @GetMapping("/me")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
        public ResponseEntity<UserProfileResponse> getCurrentUserProfile(
                        @AuthenticationPrincipal UserDetails userDetails) {
                validatePrincipal(userDetails);

                return ResponseEntity.ok(
                                userService.getCurrentUserProfile(
                                                userDetails.getUsername()));
        }

        @Operation(summary = "Cập nhật hồ sơ cá nhân")

        @PutMapping("/me")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
        public ResponseEntity<UserProfileResponse> updateCurrentUserProfile(
                        @AuthenticationPrincipal UserDetails userDetails,

                        @Valid @RequestBody UpdateProfileRequest request) {
                validatePrincipal(userDetails);

                return ResponseEntity.ok(
                                userService.updateCurrentUserProfile(
                                                userDetails.getUsername(),
                                                request));
        }

        @Operation(summary = "Cập nhật ảnh đại diện")

        @PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
        public ResponseEntity<UserProfileResponse> updateCurrentUserAvatar(
                        @AuthenticationPrincipal UserDetails userDetails,

                        @RequestParam("file") MultipartFile file) {
                validatePrincipal(userDetails);

                return ResponseEntity.ok(
                                userService.updateCurrentUserAvatar(
                                                userDetails.getUsername(),
                                                file));
        }

        @Operation(summary = "Đổi mật khẩu")

        @PutMapping("/me/password")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
        public ResponseEntity<Void> changeCurrentUserPassword(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @Valid @RequestBody ChangePasswordRequest request) {
                validatePrincipal(userDetails);

                userService.changeCurrentUserPassword(
                                userDetails.getUsername(),
                                request);

                return ResponseEntity
                                .noContent()
                                .build();
        }

        private void validatePrincipal(
                        UserDetails userDetails) {
                if (userDetails == null) {
                        throw new CurrentUserNotFoundException(
                                        "Bạn chưa đăng nhập hoặc token không hợp lệ");
                }
        }
}