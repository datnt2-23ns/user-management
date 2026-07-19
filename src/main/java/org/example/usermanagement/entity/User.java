package org.example.usermanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.usermanagement.converter.RoleConverter;
import org.example.usermanagement.converter.UserStatusConverter;
import org.example.usermanagement.enums.Gender;
import org.example.usermanagement.enums.Role;
import org.example.usermanagement.enums.UserStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_email",
                        columnNames = "email"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            name = "first_name",
            nullable = false,
            length = 100
    )
    private String firstName;

    @Column(
            name = "last_name",
            nullable = false,
            length = 150
    )
    private String lastName;

    @Column(
            nullable = false,
            length = 320
    )
    private String email;

    @Column(
            nullable = false,
            length = 255
    )
    private String password;

    @Column(
            name = "date_of_birth",
            nullable = false
    )
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private Gender gender;

    @Column(
            nullable = false,
            length = 255
    )
    private String address;

    @Column(
            nullable = false,
            length = 20
    )
    private String phone;

    @Column(
            name = "avatar_url",
            length = 255
    )
    private String avatarUrl;

    @Convert(converter = RoleConverter.class)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Convert(converter = UserStatusConverter.class)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    /*
     * ID của người thực hiện lần cập nhật gần nhất.
     * Khi User tự cập nhật hồ sơ:
     * updatedBy = chính user.getId().
     */
    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "locked_by")
    private Long lockedBy;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /*
     * Hibernate tự gán khi tạo bản ghi.
     */
    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    /*
     * Hibernate tự gán khi tạo và cập nhật bản ghi.
     */
    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @Transient
    public String getFullName() {
        String normalizedLastName =
                lastName == null
                        ? ""
                        : lastName.trim();

        String normalizedFirstName =
                firstName == null
                        ? ""
                        : firstName.trim();

        return (
                normalizedLastName
                        + " "
                        + normalizedFirstName
        ).trim();
    }

    @Transient
    public boolean isDeleted() {
        return status == UserStatus.DELETED;
    }

    @Transient
    public boolean isLocked() {
        return status == UserStatus.LOCKED;
    }
}