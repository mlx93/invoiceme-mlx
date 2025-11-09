package com.invoiceme.users.rejectuser;

import com.invoiceme.infrastructure.email.EmailService;
import com.invoiceme.infrastructure.persistence.User;
import com.invoiceme.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RejectUserHandler {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    @Transactional
    public void handle(RejectUserCommand command) {
        User user = userRepository.findById(command.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + command.getUserId()));
        
        user.reject();
        userRepository.save(user);
        
        // Send rejection email
        try {
            // Email service would send rejection notification
            // For now, just log
            System.out.println("User rejected: " + user.getEmail() + " - Reason: " + command.getReason());
        } catch (Exception e) {
            // Don't fail transaction if email fails
        }
    }
}

