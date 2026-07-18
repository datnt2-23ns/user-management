package org.example.usermanagement.controller;

import lombok.RequiredArgsConstructor;
import org.example.usermanagement.dto.response.UserProfileResponse;
import org.example.usermanagement.exception.CurrentUserNotFoundException;
import org.example.usermanagement.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN')"
    )
    public ResponseEntity<UserProfileResponse>
    getCurrentUserProfile(
            @AuthenticationPrincipal
            UserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new CurrentUserNotFoundException(
                    "Bạn chưa đăng nhập hoặc token không hợp lệ"
            );
        }

        UserProfileResponse response =
                userService.getCurrentUserProfile(
                        userDetails.getUsername()
                );

        return ResponseEntity.ok(response);
    }
}