package com.example.demo.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "user_mappings")
public class UserMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    // The ID is the User ID from the user-service
    @Id
    private UUID userId;

    public UserMapping() {
    }

    public UserMapping(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}