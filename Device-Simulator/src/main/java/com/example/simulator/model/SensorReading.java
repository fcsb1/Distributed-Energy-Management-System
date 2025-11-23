package com.example.simulator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a sensor reading to be sent to RabbitMQ.
 * Format matches what monitoring-service expects.
 */
public class SensorReading {

    private String timestamp;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("measurement_value")
    private Double measurementValue;

    public SensorReading() {
    }

    public SensorReading(String timestamp, String deviceId, Double measurementValue) {
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
        return "SensorReading{" +
                "timestamp='" + timestamp + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", measurementValue=" + measurementValue +
                '}';
    }
}