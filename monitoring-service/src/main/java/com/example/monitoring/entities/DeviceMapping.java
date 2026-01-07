package com.example.monitoring.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "device_mappings")
public class DeviceMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    // The ID is the Device ID from the device-service
    @Id
    private UUID deviceId;
    private Double maxHourlyConsumption;
    private UUID userId;

    public DeviceMapping() {
    }

    public DeviceMapping(UUID deviceId, Double maxHourlyConsumption, UUID userId) {
        this.deviceId = deviceId;
        this.maxHourlyConsumption = maxHourlyConsumption;
        this.userId = userId;

    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setMaxHourlyConsumption(Double maxHourlyConsumption) {
        this.maxHourlyConsumption = maxHourlyConsumption;
    }

    public Double getMaxHourlyConsumption() {return maxHourlyConsumption;    }
    public UUID getDeviceId() {return deviceId;}
    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }
}