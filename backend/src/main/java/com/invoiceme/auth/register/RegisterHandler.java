package com.invoiceme.auth.register;

import com.invoiceme.infrastructure.persistence.User;
import com.invoiceme.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterHandler {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public User handle(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        // Create new user
        User user = new User();
        user.email = request.getEmail();
        user.passwordHash = passwordEncoder.encode(request.getPassword());
        user.fullName = request.getFirstName() + " " + request.getLastName();
        user.role = User.UserRole.SALES; // Default role, can be changed by admin
        user.status = User.UserStatus.PENDING; // Requires approval
        
        return userRepository.save(user);
    }
}

