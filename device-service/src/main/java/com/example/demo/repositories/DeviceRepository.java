package com.example.demo.repositories;

import com.example.demo.entities.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

    // This fulfills the client role requirement by joining on the mapping table
    @Query("SELECT d FROM Device d WHERE d.userMapping.userId = :userId")
    List<Device> findDevicesByUserId(@Param("userId") UUID userId);
}