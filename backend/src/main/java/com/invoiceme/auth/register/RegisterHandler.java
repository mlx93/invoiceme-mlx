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
        
        // Extract firstName and lastName from fullName
        String fullName = request.getFullName();
        String firstName;
        String lastName;
        
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] nameParts = fullName.trim().split("\\s+", 2);
            firstName = nameParts[0];
            lastName = nameParts.length > 1 ? nameParts[1] : "";
        } else if (request.getFirstName() != null && request.getLastName() != null) {
            // Fallback to firstName/lastName if provided (backward compatibility)
            firstName = request.getFirstName();
            lastName = request.getLastName();
            fullName = firstName + " " + lastName;
        } else {
            throw new IllegalArgumentException("Full name is required");
        }
        
        // Create new user using factory method
        User user = User.create(
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            fullName,
            User.UserRole.SALES // Default role, can be changed by admin
        );
        
        return userRepository.save(user);
    }
}

