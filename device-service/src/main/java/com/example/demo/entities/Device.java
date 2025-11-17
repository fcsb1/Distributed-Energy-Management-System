package com.example.demo.entities;

import jakarta.persistence.*; // Import all
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "devices")
public class Device implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "max_consumption", nullable = false)
    private Double maxConsumption;

    // This is the Foreign Key to "Table 1"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // This will be the FK column
    @OnDelete(action = OnDeleteAction.SET_NULL) // If user is deleted, set this to null
    private UserMapping userMapping; // Can be null if unassigned

    public Device() {
    }

    //getters and Setters

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getMaxConsumption() { return maxConsumption; }
    public void setMaxConsumption(Double maxConsumption) { this.maxConsumption = maxConsumption; }

    public UserMapping getUserMapping() { return userMapping; }
    public void setUserMapping(UserMapping userMapping) { this.userMapping = userMapping; }
}