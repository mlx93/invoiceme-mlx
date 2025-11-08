package com.invoiceme.auth.login;

import com.invoiceme.infrastructure.persistence.User;
import com.invoiceme.infrastructure.persistence.UserRepository;
import com.invoiceme.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginHandler {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    
    public LoginResponse handle(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new IllegalStateException("User account is not active. Status: " + user.getStatus());
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        String token = tokenProvider.generateToken(
            user.getId(),
            user.getEmail(),
            user.getRole().name()
        );
        
        return LoginResponse.builder()
            .token(token)
            .userId(user.getId())
            .email(user.getEmail())
            .role(user.getRole().name())
            .fullName(user.getFullName())
            .build();
    }
}

