package com.example.demo.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "credentials")
public class Credential implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private String role;

    // This will link to the ID in the 'users' table
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    public Credential() {}

    public Credential(String username, String password, String role, UUID userId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.userId = userId;
    }


    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}