package com.example.monitoring.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for device synchronization messages from device-service.
 * Used when devices are created/deleted.
 */
public class DeviceSyncDTO {

    @JsonProperty("event_type")
    private String eventType; // "CREATED" or "DELETED"

    @JsonProperty("device_id")
    private String deviceId;


    public DeviceSyncDTO() {
    }

    public DeviceSyncDTO(String eventType, String deviceId) {
        this.eventType = eventType;
        this.deviceId = deviceId;
    }

    // Getters and Setters
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return "DeviceSyncDTO{" +
                "eventType='" + eventType + '\'' +
                ", deviceId='" + deviceId +
                '}';
    }
}