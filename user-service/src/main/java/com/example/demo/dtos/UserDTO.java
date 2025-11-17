package com.example.demo.dtos;

import java.util.UUID;

public class UserDTO {
    private UUID id;
    private String username;

    public UserDTO() {}

    public UserDTO(UUID id, String username) {
        this.id = id;
        this.username = username;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}