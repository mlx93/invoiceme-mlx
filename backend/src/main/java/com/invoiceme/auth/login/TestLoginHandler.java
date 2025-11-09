package com.invoiceme.auth.login;

import com.invoiceme.infrastructure.persistence.User;
import com.invoiceme.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("test-password")
@RequiredArgsConstructor
public class TestLoginHandler {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Bean
    @ConditionalOnProperty(name = "test.password", havingValue = "true")
    public CommandLineRunner testPassword() {
        return args -> {
            log.info("=== Testing Password Hash ===");
            
            String email = "admin@invoiceme.com";
            var userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                log.error("User not found: {}", email);
                return;
            }
            
            User user = userOpt.get();
            log.info("Found user: {}", user.getEmail());
            log.info("User role: {}", user.getRole());
            log.info("User status: {}", user.getStatus());
            log.info("Password hash: {}", user.getPasswordHash());
            
            // Test passwords
            String[] passwords = {"Admin123!", "password123!", "admin", "Admin123"};
            for (String pwd : passwords) {
                boolean matches = passwordEncoder.matches(pwd, user.getPasswordHash());
                log.info("Password '{}' matches: {}", pwd, matches);
            }
            
            // Generate new hash for Admin123!
            String newHash = passwordEncoder.encode("Admin123!");
            log.info("New hash for Admin123!: {}", newHash);
            
            System.exit(0);
        };
    }
}
