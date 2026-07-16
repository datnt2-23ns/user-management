package org.example.usermanagement.service.impl;

import org.example.usermanagement.dto.request.RegisterRequest;
import org.example.usermanagement.dto.response.RegisterResponse;
import org.example.usermanagement.entity.User;
import org.example.usermanagement.enums.Role;
import org.example.usermanagement.enums.UserStatus;
import org.example.usermanagement.exception.EmailAlreadyExistsException;
import org.example.usermanagement.repository.UserRepository;
import org.example.usermanagement.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String normalizedEmail = request
                .getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyExistsException(
                    "Email đã tồn tại trong hệ thống"
            );
        }

        User user = User.builder()
                .firstName(normalizeText(request.getFirstName()))
                .lastName(normalizeText(request.getLastName()))
                .email(normalizedEmail)
                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )

                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .address(normalizeText(request.getAddress()))
                .phone(request.getPhone().trim())
                .avatarUrl(null)
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.saveAndFlush(user);

        return toRegisterResponse(savedUser);
    }

    private RegisterResponse toRegisterResponse(User user) {
        return RegisterResponse.builder()
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
                .build();
    }

    private String normalizeText(String value) {
        return value
                .trim()
                .replaceAll("\\s+", " ");
    }
}