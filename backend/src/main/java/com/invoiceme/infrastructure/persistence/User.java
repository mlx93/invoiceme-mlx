package com.invoiceme.infrastructure.persistence;

import jakarta.persistence.*;
import jakarta.persistence.Convert;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;
    
    @Convert(converter = UserRoleConverter.class)
    @Column(name = "role", nullable = false, columnDefinition = "user_role_enum")
    @org.hibernate.annotations.ColumnTransformer(
        read = "role::text",
        write = "?::user_role_enum"
    )
    private UserRole role;
    
    @Convert(converter = UserStatusConverter.class)
    @Column(name = "status", nullable = false, columnDefinition = "user_status_enum")
    @org.hibernate.annotations.ColumnTransformer(
        read = "status::text",
        write = "?::user_status_enum"
    )
    private UserStatus status;
    
    @Column(name = "customer_id")
    private UUID customerId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    // Factory method for creating new users
    public static User create(String email, String passwordHash, String fullName, UserRole role) {
        User user = new User();
        user.email = email;
        user.passwordHash = passwordHash;
        user.fullName = fullName;
        user.role = role;
        user.status = UserStatus.PENDING;
        return user;
    }
    
    // Factory method for creating active users (e.g., when admin creates customer)
    public static User createActive(String email, String passwordHash, String fullName, UserRole role, UUID customerId) {
        User user = new User();
        user.email = email;
        user.passwordHash = passwordHash;
        user.fullName = fullName;
        user.role = role;
        user.status = UserStatus.ACTIVE;
        user.customerId = customerId;
        return user;
    }
    
    // Methods to change status
    public void approve() {
        if (this.status != UserStatus.PENDING) {
            throw new IllegalStateException("Can only approve PENDING users. Current status: " + this.status);
        }
        this.status = UserStatus.ACTIVE;
    }
    
    public void reject() {
        if (this.status != UserStatus.PENDING) {
            throw new IllegalStateException("Can only reject PENDING users. Current status: " + this.status);
        }
        this.status = UserStatus.INACTIVE;
    }
    
    public enum UserRole {
        SYSADMIN, ACCOUNTANT, SALES, CUSTOMER
    }
    
    public enum UserStatus {
        PENDING, ACTIVE, INACTIVE, LOCKED
    }
}

