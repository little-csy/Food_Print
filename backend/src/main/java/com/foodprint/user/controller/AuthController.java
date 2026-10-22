package com.foodprint.user.controller;

import com.foodprint.user.dto.PasswordResetRequest;
import com.foodprint.user.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/forgot-password
     * Public endpoint. Triggers a Supabase password-reset email.
     * Always returns 200 to prevent email enumeration.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody PasswordResetRequest request) {
        try {
            authService.sendPasswordResetEmail(request.getEmail());
        } catch (Exception e) {
            log.error("Password reset request failed for email {}: {}", request.getEmail(), e.getMessage());
        }
        return ResponseEntity.ok(Map.of("message", "If that email is registered, a reset link has been sent."));
    }
}
