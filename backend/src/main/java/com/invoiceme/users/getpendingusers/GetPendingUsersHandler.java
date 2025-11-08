package com.invoiceme.users.getpendingusers;

import com.invoiceme.infrastructure.persistence.User;
import com.invoiceme.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetPendingUsersHandler {
    
    private final UserRepository userRepository;
    
    public List<PendingUserDto> handle(GetPendingUsersQuery query) {
        List<User> pendingUsers = userRepository.findAll().stream()
            .filter(user -> user.getStatus() == User.UserStatus.PENDING)
            .collect(Collectors.toList());
        
        return pendingUsers.stream()
            .map(user -> PendingUserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build())
            .collect(Collectors.toList());
    }
}

