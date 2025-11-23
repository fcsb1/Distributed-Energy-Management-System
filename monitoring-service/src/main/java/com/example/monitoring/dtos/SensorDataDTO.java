package com.example.monitoring.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing sensor data from the Device Data Simulator.
 * Message format: {timestamp, device_id, measurement_value}
 */
public class SensorDataDTO {

    private String timestamp;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("measurement_value")
    private Double measurementValue;

    public SensorDataDTO() {
    }

    public SensorDataDTO(String timestamp, String deviceId, Double measurementValue) {
        this.timestamp = timestamp;
        this.deviceId = deviceId;
        this.measurementValue = measurementValue;
    }

    // Getters and Setters
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Double getMeasurementValue() {
        return measurementValue;
    }

    public void setMeasurementValue(Double measurementValue) {
        this.measurementValue = measurementValue;
    }

    @Override
    public String toString() {
        return "SensorDataDTO{" +
                "timestamp='" + timestamp + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", measurementValue=" + measurementValue +
                '}';
    }
}