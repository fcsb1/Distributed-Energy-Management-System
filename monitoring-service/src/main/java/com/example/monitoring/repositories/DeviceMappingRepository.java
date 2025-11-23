package com.example.monitoring.repositories;

import com.example.monitoring.entities.DeviceMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeviceMappingRepository extends JpaRepository<DeviceMapping, UUID> {
    boolean existsByDeviceId(UUID deviceId);
}