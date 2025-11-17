package com.example.demo.services;

import com.example.demo.dtos.LoginRequestDTO;
import com.example.demo.dtos.LoginResponseDTO;
import com.example.demo.dtos.RegisterDTO; // We will create this DTO
import com.example.demo.entities.Credential;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.CredentialRepository;
import com.example.demo.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(CredentialRepository credentialRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        Credential user = credentialRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> {
                    LOGGER.warn("Login attempt for non-existent user: {}", loginRequest.getUsername());
                    return new ResourceNotFoundException("Invalid credentials");
                });

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            LOGGER.warn("Invalid password attempt for user: {}", loginRequest.getUsername());
            throw new ResourceNotFoundException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user);
        LOGGER.info("User {} logged in successfully", user.getUsername());
        return new LoginResponseDTO(token);
    }

    // New "Register" method as required by a1.pdf
    public UUID register(RegisterDTO registerDTO) {
        credentialRepository.findByUsername(registerDTO.getUsername()).ifPresent(u -> {
            throw new DataIntegrityViolationException("Username '" + registerDTO.getUsername() + "' is already taken.");
        });

        if (credentialRepository.existsByUserId(registerDTO.getUserId())) {
            throw new DataIntegrityViolationException("User ID '" + registerDTO.getUserId() + "' is already registered.");
        }

        Credential credential = new Credential(
                registerDTO.getUsername(),
                passwordEncoder.encode(registerDTO.getPassword()),
                registerDTO.getRole(),
                registerDTO.getUserId()
        );

        credential = credentialRepository.save(credential);
        LOGGER.info("Credential created for user ID: {}", credential.getUserId());
        return credential.getId();
    }



    @Transactional
    public void updateCredential(UUID userId, RegisterDTO registerDTO) {
        // Find the existing credential by the User ID
        Credential credential = credentialRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found for user ID: " + userId));

        // Check if username is being changed and if the new one is already taken
        if (!credential.getUsername().equals(registerDTO.getUsername())) {
            credentialRepository.findByUsername(registerDTO.getUsername()).ifPresent(u -> {
                throw new DataIntegrityViolationException("Username '" + registerDTO.getUsername() + "' is already taken.");
            });
            credential.setUsername(registerDTO.getUsername());
        }

        // Update fields
        credential.setRole(registerDTO.getRole());
        // Only update password if a new one is provided
        if (registerDTO.getPassword() != null && !registerDTO.getPassword().isBlank()) {
            credential.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        }

        credentialRepository.save(credential);
        LOGGER.info("Credential updated for user ID: {}", userId);
    }

    // New "Delete" method for syncing with user-service
    @Transactional
    public void deleteCredential(UUID userId) {
        if (!credentialRepository.existsByUserId(userId)) {
            LOGGER.warn("No credential found to delete for user ID: {}", userId);
            return;
        }
        credentialRepository.deleteByUserId(userId);
        LOGGER.info("Credential deleted for user ID: {}", userId);
    }

    // New "Validate Token" method
    public boolean validate(String token) {
        return jwtUtil.validateToken(token);
    }

    // New "Validate Role" method
    public boolean validateRole(String token, String requiredRole) {
        if (!validate(token)) {
            return false;
        }
        String actualRole = jwtUtil.getRoleFromToken(token);
        return actualRole != null && actualRole.equals(requiredRole);
    }
}