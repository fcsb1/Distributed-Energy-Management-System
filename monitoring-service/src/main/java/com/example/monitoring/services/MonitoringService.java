package com.example.monitoring.services;

import com.example.monitoring.dtos.HourlyConsumptionDTO;
import com.example.monitoring.entities.HourlyConsumption;
import com.example.monitoring.repositories.HourlyConsumptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MonitoringService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringService.class);

    private final HourlyConsumptionRepository hourlyConsumptionRepository;

    public MonitoringService(HourlyConsumptionRepository hourlyConsumptionRepository) {
        this.hourlyConsumptionRepository = hourlyConsumptionRepository;
    }

    /**
     * Get hourly consumption data for a device on a specific date.
     */
    public List<HourlyConsumptionDTO> getHourlyConsumptionByDeviceAndDate(UUID deviceId, LocalDate date) {
        LOGGER.debug("Fetching hourly consumption for deviceId={}, date={}", deviceId, date);

        List<HourlyConsumption> records = hourlyConsumptionRepository.findByDeviceIdAndDate(deviceId, date);

        return records.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert entity to DTO
     */
    private HourlyConsumptionDTO toDTO(HourlyConsumption entity) {
        return new HourlyConsumptionDTO(
                entity.getId(),
                entity.getDeviceId(),
                entity.getDate(),
                entity.getHour(),
                entity.getTotalConsumption()
        );
    }
}