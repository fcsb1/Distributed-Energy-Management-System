package com.example.monitoring.repositories;

import com.example.monitoring.entities.HourlyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HourlyConsumptionRepository extends JpaRepository<HourlyConsumption, UUID> {

    // Find all hourly data for a device on a specific date (for charts)
    @Query("SELECT h FROM HourlyConsumption h WHERE h.deviceId = :deviceId AND h.date = :date ORDER BY h.hour ASC")
    List<HourlyConsumption> findByDeviceIdAndDate(
            @Param("deviceId") UUID deviceId,
            @Param("date") LocalDate date
    );

    // Find specific hour record (for upsert logic)
    Optional<HourlyConsumption> findByDeviceIdAndDateAndHour(UUID deviceId, LocalDate date, Integer hour);

    // Delete all data for a device (when device is deleted)
    void deleteByDeviceId(UUID deviceId);
}