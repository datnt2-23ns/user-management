package org.example.usermanagement.security;

import lombok.RequiredArgsConstructor;
import org.example.usermanagement.entity.User;
import org.example.usermanagement.enums.UserStatus;
import org.example.usermanagement.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        String normalizedEmail = normalizeEmail(email);

        User user = userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Không tìm thấy tài khoản với email đã cung cấp"
                        )
                );

        boolean locked =
                user.getStatus() == UserStatus.LOCKED;

        boolean deleted =
                user.getStatus() == UserStatus.DELETED;

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(
                        "ROLE_" + user.getRole().name()
                )
                .accountLocked(locked)
                .disabled(deleted)
                .build();
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}