package org.example.usermanagement.repository;

import org.example.usermanagement.entity.User;
import org.example.usermanagement.enums.Role;
import org.example.usermanagement.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository
                extends JpaRepository<User, Long>,
                JpaSpecificationExecutor<User> {

        Optional<User> findByEmailIgnoreCase(
                        String email);

        boolean existsByEmailIgnoreCase(
                        String email);

        long countByRoleAndStatus(
                        Role role,
                        UserStatus status);
}