package com.example.demo.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * DTO for device synchronization messages sent to RabbitMQ.
 * Used to notify monitoring-service about device changes.
 */
public class DeviceSyncMessageDTO {

    @JsonProperty("event_type")
    private String eventType; // "CREATED" or "DELETED"

    @JsonProperty("device_id")
    private String deviceId;

    public DeviceSyncMessageDTO() {
    }

    public DeviceSyncMessageDTO(String eventType, UUID deviceId) {
        this.eventType = eventType;
        this.deviceId = deviceId.toString();
    }

    // Static factory methods for convenience
    public static DeviceSyncMessageDTO created(UUID deviceId) {
        return new DeviceSyncMessageDTO("CREATED", deviceId);
    }

    public static DeviceSyncMessageDTO deleted(UUID deviceId) {
        return new DeviceSyncMessageDTO("DELETED", deviceId);
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
        return "DeviceSyncMessageDTO{" +
                "eventType='" + eventType + '\'' +
                ", deviceId='" + deviceId +
                '}';
    }
}