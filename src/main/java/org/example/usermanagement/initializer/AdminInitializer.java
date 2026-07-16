package org.example.usermanagement.initializer;

import org.example.usermanagement.entity.User;
import org.example.usermanagement.enums.Gender;
import org.example.usermanagement.enums.Role;
import org.example.usermanagement.enums.UserStatus;
import org.example.usermanagement.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.first-name}")
    private String adminFirstName;

    @Value("${app.admin.last-name}")
    private String adminLastName;

    @Value("${app.admin.date-of-birth}")
    private String adminDateOfBirth;

    @Value("${app.admin.gender}")
    private String adminGender;

    @Value("${app.admin.address}")
    private String adminAddress;

    @Value("${app.admin.phone}")
    private String adminPhone;

    @Override
    public void run(String... args) {
        String normalizedEmail = adminEmail
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            log.info(
                    "Initial admin account already exists: {}",
                    normalizedEmail
            );
            return;
        }

        Gender gender = Gender.valueOf(
                adminGender
                        .trim()
                        .toUpperCase(Locale.ROOT)
        );

        User admin = User.builder()
                .firstName(adminFirstName.trim())
                .lastName(adminLastName.trim())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(adminPassword))
                .dateOfBirth(LocalDate.parse(adminDateOfBirth))
                .gender(gender)
                .address(adminAddress.trim())
                .phone(adminPhone.trim())
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(admin);

        log.info(
                "Initial admin account created: {}",
                normalizedEmail
        );
    }
}