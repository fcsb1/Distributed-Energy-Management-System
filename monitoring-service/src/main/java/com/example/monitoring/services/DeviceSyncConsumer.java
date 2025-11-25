package com.example.monitoring.services;

import com.example.monitoring.entities.DeviceMapping;
import com.example.monitoring.repositories.DeviceMappingRepository;
import com.example.monitoring.repositories.HourlyConsumptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class DeviceSyncConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceSyncConsumer.class);
    private final DeviceMappingRepository deviceMappingRepository;
    private final HourlyConsumptionRepository hourlyConsumptionRepository;
    private final ObjectMapper objectMapper;

    public DeviceSyncConsumer(DeviceMappingRepository deviceMappingRepository,
                              HourlyConsumptionRepository hourlyConsumptionRepository,
                              ObjectMapper objectMapper) {
        this.deviceMappingRepository = deviceMappingRepository;
        this.hourlyConsumptionRepository = hourlyConsumptionRepository;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "monitoring.device.sync.queue")
    @Transactional
    public void receiveDeviceSyncMessage(String jsonMessage) {
        try {
            LOGGER.debug("Received device sync message: {}", jsonMessage);

            // 1. Raw String to Map
            Map<String, Object> map = objectMapper.readValue(jsonMessage, Map.class);

            // 2. Manual Extraction
            String action = (String) map.get("action");
            String deviceIdStr = (String) map.get("deviceId");
            UUID deviceId = UUID.fromString(deviceIdStr);

            if ("CREATED".equalsIgnoreCase(action)) {
                handleDeviceCreated(deviceId);
            } else if ("DELETED".equalsIgnoreCase(action)) {
                handleDeviceDeleted(deviceId);
            } else {
                LOGGER.warn("Unknown event action: {}", action);
            }

        } catch (Exception e) {
            LOGGER.error("Error processing device sync message", e);
        }
    }

    private void handleDeviceCreated(UUID deviceId) {
        if (deviceMappingRepository.existsByDeviceId(deviceId)) {
            return;
        }
        deviceMappingRepository.save(new DeviceMapping(deviceId));
        LOGGER.info("Synced: Created device mapping for {}", deviceId);
    }

    private void handleDeviceDeleted(UUID deviceId) {
        if (deviceMappingRepository.existsById(deviceId)) {
            deviceMappingRepository.deleteById(deviceId);
        }
        hourlyConsumptionRepository.deleteByDeviceId(deviceId);
        LOGGER.info("Synced: Deleted device mapping and data for {}", deviceId);
    }
}