package com.invoiceme.users.approveuser;

import com.invoiceme.infrastructure.email.EmailService;
import com.invoiceme.infrastructure.persistence.User;
import com.invoiceme.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApproveUserHandler {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    @Transactional
    public User handle(ApproveUserCommand command) {
        User user = userRepository.findById(command.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + command.getUserId()));
        
        if (user.getStatus() != User.UserStatus.PENDING) {
            throw new IllegalStateException("Can only approve PENDING users. Current status: " + user.getStatus());
        }
        
        user.status = User.UserStatus.ACTIVE;
        User savedUser = userRepository.save(user);
        
        // Send approval email
        try {
            // Email service would send approval notification
            // For now, just log
            System.out.println("User approved: " + user.getEmail());
        } catch (Exception e) {
            // Don't fail transaction if email fails
        }
        
        return savedUser;
    }
}

