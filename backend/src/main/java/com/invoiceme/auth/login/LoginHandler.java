package com.invoiceme.auth.login;

import com.invoiceme.infrastructure.persistence.User;
import com.invoiceme.infrastructure.persistence.UserRepository;
import com.invoiceme.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginHandler {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    
    public LoginResponse handle(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        
        try {
            var userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                log.warn("User not found: {}", request.getEmail());
                throw new IllegalArgumentException("Invalid email or password");
            }
            
            User user = userOpt.get();
            log.info("User found: {}, role: {}, status: {}", user.getEmail(), user.getRole(), user.getStatus());
            
            if (user.getStatus() != User.UserStatus.ACTIVE) {
                log.warn("User account not active: {}, status: {}", user.getEmail(), user.getStatus());
                throw new IllegalStateException("User account is not active. Status: " + user.getStatus());
            }
            
            boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
            log.info("Password match result: {}", passwordMatches);
            
            if (!passwordMatches) {
                log.warn("Password mismatch for user: {}", user.getEmail());
                throw new IllegalArgumentException("Invalid email or password");
            }
            
            log.info("Generating JWT token for user: {}", user.getEmail());
            String token = tokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
            );
            log.info("JWT token generated successfully for user: {}", user.getEmail());
            
            return LoginResponse.builder()
                .token(token)
                .user(LoginResponse.AuthenticatedUser.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole().name())
                    .status(user.getStatus().name())
                    .customerId(user.getCustomerId())
                    .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                    .build())
                .build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Re-throw validation errors
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login for email: {}", request.getEmail(), e);
            throw new RuntimeException("Login failed due to an unexpected error", e);
        }
    }
}

