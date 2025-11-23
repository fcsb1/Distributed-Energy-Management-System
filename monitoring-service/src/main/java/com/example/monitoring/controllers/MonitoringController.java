package com.example.monitoring.controllers;

import com.example.monitoring.dtos.HourlyConsumptionDTO;
import com.example.monitoring.services.MonitoringService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/monitoring")
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    /**
     * Get hourly energy consumption for a device on a specific date.
     * Frontend will use this to render line/bar charts.
     *
     * Example: GET /monitoring/device/{deviceId}/consumption?date=2025-01-15
     */
    @GetMapping("/device/{deviceId}/consumption")
    public ResponseEntity<List<HourlyConsumptionDTO>> getHourlyConsumption(
            @PathVariable UUID deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<HourlyConsumptionDTO> data = monitoringService.getHourlyConsumptionByDeviceAndDate(deviceId, date);
        return ResponseEntity.ok(data);
    }
}