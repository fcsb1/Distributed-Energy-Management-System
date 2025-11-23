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

    public DeviceMapping() {
    }

    public DeviceMapping(UUID deviceId) {
        this.deviceId = deviceId;

    }

    // Getters and Setters
    public UUID getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }
}