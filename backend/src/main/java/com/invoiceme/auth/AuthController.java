package com.invoiceme.auth;

import com.invoiceme.auth.login.LoginHandler;
import com.invoiceme.auth.login.LoginRequest;
import com.invoiceme.auth.login.LoginResponse;
import com.invoiceme.auth.register.RegisterHandler;
import com.invoiceme.auth.register.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final LoginHandler loginHandler;
    private final RegisterHandler registerHandler;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginHandler.handle(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        registerHandler.handle(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

