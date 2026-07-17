package org.example.usermanagement.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.usermanagement.dto.request.LoginRequest;
import org.example.usermanagement.dto.request.RegisterRequest;
import org.example.usermanagement.dto.response.LoginResponse;
import org.example.usermanagement.dto.response.RegisterResponse;
import org.example.usermanagement.entity.User;
import org.example.usermanagement.enums.Role;
import org.example.usermanagement.enums.UserStatus;
import org.example.usermanagement.exception.EmailAlreadyExistsException;
import org.example.usermanagement.exception.InvalidCredentialsException;
import org.example.usermanagement.repository.UserRepository;
import org.example.usermanagement.security.JwtUtils;
import org.example.usermanagement.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

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

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        Authentication authentication;

        try {
            authentication =
                    authenticationManager.authenticate(
                            UsernamePasswordAuthenticationToken
                                    .unauthenticated(
                                            normalizedEmail,
                                            request.getPassword()
                                    )
                    );
        } catch (LockedException exception) {
            throw exception;
        } catch (DisabledException exception) {
            throw exception;
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException(
                    "Email hoặc mật khẩu không đúng"
            );
        }

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        User user = userRepository
                .findByEmailIgnoreCase(
                        userDetails.getUsername()
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Không tìm thấy người dùng sau khi xác thực"
                        )
                );

        String accessToken =
                jwtUtils.generateToken(userDetails);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(
                        jwtUtils.getExpirationSeconds()
                )
                .user(
                        LoginResponse.UserInfo.builder()
                                .id(user.getId())
                                .firstName(user.getFirstName())
                                .lastName(user.getLastName())
                                .fullName(user.getFullName())
                                .email(user.getEmail())
                                .gender(user.getGender())
                                .avatarUrl(user.getAvatarUrl())
                                .role(user.getRole())
                                .status(user.getStatus())
                                .build()
                )
                .build();
    }
}