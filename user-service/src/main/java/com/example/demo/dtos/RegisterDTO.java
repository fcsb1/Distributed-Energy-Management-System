package com.example.demo.dtos;

// This DTO is used to send data to the auth-service
public class RegisterDTO {

    private String username;
    private String password;
    private String role;
    private java.util.UUID userId;

    public RegisterDTO(String username, String password, String role, java.util.UUID userId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.userId = userId;
    }

    //etters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public java.util.UUID getUserId() { return userId; }
    public void setUserId(java.util.UUID userId) { this.userId = userId; }
}