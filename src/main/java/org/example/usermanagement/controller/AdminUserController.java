package org.example.usermanagement.controller;

import lombok.RequiredArgsConstructor;
import org.example.usermanagement.dto.response.AdminUserDetailResponse;
import org.example.usermanagement.dto.response.AdminUserListItemResponse;
import org.example.usermanagement.dto.response.PageResponse;
import org.example.usermanagement.service.AdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

        private final AdminUserService adminUserService;

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

        @GetMapping("/{userId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<AdminUserDetailResponse> getUserById(
                        @PathVariable Long userId) {
                return ResponseEntity.ok(
                                adminUserService.getUserById(userId));
        }
}