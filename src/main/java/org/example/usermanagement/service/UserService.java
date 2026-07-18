package org.example.usermanagement.service;

import org.example.usermanagement.dto.response.UserProfileResponse;

public interface UserService {

    UserProfileResponse getCurrentUserProfile(
            String email
    );
}