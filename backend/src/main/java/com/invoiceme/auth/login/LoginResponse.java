package com.invoiceme.auth.login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String token;
    private AuthenticatedUser user;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthenticatedUser {
        private UUID id;
        private String email;
        private String fullName;
        private String role;
        private String status;
        private UUID customerId;
        private String createdAt;
    }
}

