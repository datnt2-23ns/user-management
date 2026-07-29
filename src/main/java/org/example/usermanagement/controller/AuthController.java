package org.example.usermanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.usermanagement.dto.request.LoginRequest;
import org.example.usermanagement.dto.request.RegisterRequest;
import org.example.usermanagement.dto.response.LoginResponse;
import org.example.usermanagement.dto.response.RegisterResponse;
import org.example.usermanagement.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Xác thực", description = "Đăng ký và đăng nhập tài khoản")
public class AuthController {

        private final AuthService authService;

        @Operation(summary = "Đăng ký tài khoản", description = "Tạo tài khoản mới với vai trò USER và trạng thái ACTIVE")

        @PostMapping("/register")
        public ResponseEntity<RegisterResponse> register(
                        @Valid @RequestBody RegisterRequest request) {
                RegisterResponse response = authService.register(request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response);
        }

        @Operation(summary = "Đăng nhập", description = "Xác thực email, mật khẩu và trả về JWT access token")

        @PostMapping("/login")
        public ResponseEntity<LoginResponse> login(
                        @Valid @RequestBody LoginRequest request) {
                return ResponseEntity.ok(
                                authService.login(request));
        }
}