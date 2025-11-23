package com.example.monitoring.dtos;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for returning hourly consumption data to the frontend.
 */
public class HourlyConsumptionDTO {

    private UUID id;
    private UUID deviceId;
    private LocalDate date;
    private Integer hour;           // 0-23
    private Double totalConsumption; // kWh

    public HourlyConsumptionDTO() {
    }

    public HourlyConsumptionDTO(UUID id, UUID deviceId, LocalDate date, Integer hour,
                                Double totalConsumption) {
        this.id = id;
        this.deviceId = deviceId;
        this.date = date;
        this.hour = hour;
        this.totalConsumption = totalConsumption;

    }

    //Gett and Set
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public Double getTotalConsumption() {
        return totalConsumption;
    }

    public void setTotalConsumption(Double totalConsumption) {
        this.totalConsumption = totalConsumption;
    }
}