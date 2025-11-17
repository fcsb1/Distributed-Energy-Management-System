package com.example.demo.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class DeviceDetailsDTO {

    private UUID id;

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "maxConsumption is required")
    private Double maxConsumption;

    //userId can be null
    private UUID userId;

    public DeviceDetailsDTO() {
    }

    public DeviceDetailsDTO(UUID id, String name, Double maxConsumption, UUID userId) {
        this.id = id;
        this.name = name;
        this.maxConsumption = maxConsumption;
        this.userId = userId;
    }



    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getMaxConsumption() { return maxConsumption; }
    public void setMaxConsumption(Double maxConsumption) { this.maxConsumption = maxConsumption; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}