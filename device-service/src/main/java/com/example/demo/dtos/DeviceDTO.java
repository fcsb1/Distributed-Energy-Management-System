package com.example.demo.dtos;

import java.util.UUID;

public class DeviceDTO {
    private UUID id;
    private String name;
    private Double maxConsumption;
    private UUID userId;

    public DeviceDTO() {}

    public DeviceDTO(UUID id, String name, Double maxConsumption, UUID userId) {
        this.id = id;
        this.name = name;
        this.maxConsumption = maxConsumption;
        this.userId = userId;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getMaxConsumption() { return maxConsumption; }
    public void setMaxConsumption(Double maxConsumption) { this.maxConsumption = maxConsumption; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}