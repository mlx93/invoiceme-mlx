package com.invoiceme.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePasswordHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "Admin123!";
        
        // Generate hash
        String hash = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        
        // Verify it matches
        boolean matches = encoder.matches(password, hash);
        System.out.println("Verification: " + matches);
        
        // Test with the stored hash
        String storedHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        System.out.println("\nTesting stored hash:");
        System.out.println("Admin123! matches: " + encoder.matches("Admin123!", storedHash));
        System.out.println("password123! matches: " + encoder.matches("password123!", storedHash));
    }
}
