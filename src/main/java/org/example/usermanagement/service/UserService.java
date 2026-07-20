package org.example.usermanagement.service;

import org.example.usermanagement.dto.request.UpdateProfileRequest;
import org.example.usermanagement.dto.response.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;
import org.example.usermanagement.dto.request.ChangePasswordRequest;

public interface UserService {

        UserProfileResponse getCurrentUserProfile(
                        String email);

        UserProfileResponse updateCurrentUserProfile(
                        String email,
                        UpdateProfileRequest request);

        UserProfileResponse updateCurrentUserAvatar(
                        String email,
                        MultipartFile file);

        void changeCurrentUserPassword(
                        String email,
                        ChangePasswordRequest request);
}