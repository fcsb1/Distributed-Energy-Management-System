package com.example.demo.dtos;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
//what frontend needs to send to create a user
public class UserDetailsDTO {

    private UUID id;

    @NotBlank(message = "username is required")
    private String username;

    // These fields are for the auth-service
    private String password;

    @NotBlank(message = "role is required")
    private String role; // "ROLE_ADMIN" or "ROLE_CLIENT"

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}