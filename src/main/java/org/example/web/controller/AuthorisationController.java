package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.domain.service.AuthorisationService;
import org.example.web.model.request.JwtRequest;
import org.example.web.model.request.RefreshJwtRequest;
import org.example.web.model.request.SignUpRequest;
import org.example.web.model.response.JwtResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:63342")
public class AuthorisationController {
    private final AuthorisationService authorisationService;

    @Autowired
    public AuthorisationController(AuthorisationService authorisationService) {
        this.authorisationService = authorisationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody SignUpRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean success = authorisationService.register(request);
            if (success) {
                response.put("message", "User registered successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "User registration failed: already exists or invalid data");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            // Логирование: log.error("Error in /auth/signup: {}", e.getMessage());
            response.put("error", "Server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody JwtRequest request) {
        try {
            JwtResponse response = authorisationService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Логирование: log.error("Error in /auth/login: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Authentication failed: " + e.getMessage());
            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    @PostMapping("/refresh-access")
    public ResponseEntity<?> refreshAccessToken(@Valid @RequestBody RefreshJwtRequest request) {
        try {
            JwtResponse response = authorisationService.refreshAccessToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Логирование: log.error("Error in /auth/refresh-access: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to refresh access token: " + e.getMessage());
            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshJwtRequest request) {
        try {
            JwtResponse response = authorisationService.refreshRefreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Логирование: log.error("Error in /auth/refresh-token: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to refresh token: " + e.getMessage());
            return ResponseEntity.status(401).body(errorResponse);
        }
    }
}