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
        validatePrincipal(userDetails);

        return ResponseEntity.ok(
                userService.getCurrentUserProfile(
                        userDetails.getUsername()
                )
        );
    }

    @PutMapping("/me")
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN')"
    )
    public ResponseEntity<UserProfileResponse>
    updateCurrentUserProfile(
            @AuthenticationPrincipal
            UserDetails userDetails,

            @Valid
            @RequestBody
            UpdateProfileRequest request
    ) {
        validatePrincipal(userDetails);

        return ResponseEntity.ok(
                userService.updateCurrentUserProfile(
                        userDetails.getUsername(),
                        request
                )
        );
    }

    @PutMapping(
            value = "/me/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN')"
    )
    public ResponseEntity<UserProfileResponse>
    updateCurrentUserAvatar(
            @AuthenticationPrincipal
            UserDetails userDetails,

            @RequestParam("file")
            MultipartFile file
    ) {
        validatePrincipal(userDetails);

        return ResponseEntity.ok(
                userService.updateCurrentUserAvatar(
                        userDetails.getUsername(),
                        file
                )
        );
    }

    private void validatePrincipal(
            UserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new CurrentUserNotFoundException(
                    "Bạn chưa đăng nhập hoặc token không hợp lệ"
            );
        }
    }
}