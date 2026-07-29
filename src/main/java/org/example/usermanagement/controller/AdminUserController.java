package org.example.usermanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.usermanagement.dto.request.UpdateUserRoleRequest;
import org.example.usermanagement.dto.request.UpdateUserStatusRequest;
import org.example.usermanagement.dto.response.AdminUserDetailResponse;
import org.example.usermanagement.dto.response.AdminUserListItemResponse;
import org.example.usermanagement.dto.response.AdminUserRoleResponse;
import org.example.usermanagement.dto.response.AdminUserStatusResponse;
import org.example.usermanagement.dto.response.PageResponse;
import org.example.usermanagement.exception.CurrentUserNotFoundException;
import org.example.usermanagement.service.AdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Quản trị người dùng", description = "Các chức năng dành riêng cho quản trị viên")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

        private final AdminUserService adminUserService;

        @Operation(summary = "Xem danh sách tài khoản", description = "Hỗ trợ tìm kiếm, lọc, sắp xếp và phân trang")

        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<PageResponse<AdminUserListItemResponse>> getUsers(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "") String keyword,
                        @RequestParam(defaultValue = "") String role,
                        @RequestParam(defaultValue = "") String status,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String direction) {

                return ResponseEntity.ok(
                                adminUserService.getUsers(
                                                page,
                                                size,
                                                keyword,
                                                role,
                                                status,
                                                sortBy,
                                                direction));
        }

        @Operation(summary = "Xem chi tiết tài khoản")

        @GetMapping("/{userId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<AdminUserDetailResponse> getUserById(
                        @PathVariable Long userId) {

                return ResponseEntity.ok(
                                adminUserService.getUserById(userId));
        }

        @Operation(summary = "Khóa hoặc mở khóa tài khoản")

        @PatchMapping("/{userId}/status")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<AdminUserStatusResponse> updateUserStatus(
                        @PathVariable Long userId,
                        @Valid @RequestBody UpdateUserStatusRequest request,
                        @AuthenticationPrincipal UserDetails currentAdmin) {

                validateCurrentAdmin(currentAdmin);

                return ResponseEntity.ok(
                                adminUserService.updateUserStatus(
                                                userId,
                                                currentAdmin.getUsername(),
                                                request));
        }

        @Operation(summary = "Xóa tài khoản")

        @DeleteMapping("/{userId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Void> deleteUser(
                        @PathVariable Long userId,
                        @AuthenticationPrincipal UserDetails currentAdmin) {

                validateCurrentAdmin(currentAdmin);

                adminUserService.deleteUser(
                                userId,
                                currentAdmin.getUsername());

                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Thay đổi vai trò tài khoản", description = "Nâng USER thành ADMIN hoặc hạ ADMIN thành USER")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Thay đổi vai trò thành công"),
                        @ApiResponse(responseCode = "400", description = "Vai trò yêu cầu không hợp lệ"),
                        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ"),
                        @ApiResponse(responseCode = "403", description = "Không có quyền quản trị"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy tài khoản"),
                        @ApiResponse(responseCode = "409", description = "Không thể hạ Admin đang hoạt động cuối cùng"),
                        @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
        })
        @PatchMapping("/{userId}/role")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<AdminUserRoleResponse> updateUserRole(
                        @PathVariable Long userId,
                        @Valid @RequestBody UpdateUserRoleRequest request,
                        @AuthenticationPrincipal UserDetails currentAdmin) {

                validateCurrentAdmin(currentAdmin);

                return ResponseEntity.ok(
                                adminUserService.updateUserRole(
                                                userId,
                                                currentAdmin.getUsername(),
                                                request));
        }

        private void validateCurrentAdmin(UserDetails currentAdmin) {
                if (currentAdmin == null) {
                        throw new CurrentUserNotFoundException(
                                        "Không xác định được quản trị viên đang đăng nhập");
                }
        }
}