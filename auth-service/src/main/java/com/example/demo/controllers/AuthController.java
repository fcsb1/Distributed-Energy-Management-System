package com.example.demo.controllers;

import com.example.demo.dtos.LoginRequestDTO;
import com.example.demo.dtos.LoginResponseDTO;
import com.example.demo.dtos.RegisterDTO;
import com.example.demo.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/auth") // Using a base path
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "User login", description = "Authenticates user and returns JWT token")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @PostMapping("/login") // Public: /api/auth/login
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }


    // This is the endpoint for Traefik Forward Auth
    @Operation(summary = "Validate JWT token", description = "Validates token and checks role")
    @ApiResponse(responseCode = "200", description = "Token valid")
    @GetMapping("/validate") // Internal: /api/auth/validate
    public ResponseEntity<Void> validateToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        String token = authHeader.substring(7);

        // This header will be set by Traefik
        String requiredRole = request.getHeader("X-Required-Role");

        boolean isValid;
        if (requiredRole != null && !requiredRole.isEmpty()) {
            isValid = authService.validateRole(token, requiredRole);
        } else {
            isValid = authService.validate(token);
        }

        if (isValid) {
            return ResponseEntity.ok().build(); // 200 OK
        } else {
            return ResponseEntity.status(401).build(); // 401 Unauthorized
        }
    }
}