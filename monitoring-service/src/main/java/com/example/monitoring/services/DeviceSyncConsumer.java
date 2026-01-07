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

            // 1Raw String to Map
            Map<String, Object> map = objectMapper.readValue(jsonMessage, Map.class);

            // 2Manual Extraction
            String action = (String) map.get("action");
            String deviceIdStr = (String) map.get("deviceId");
            UUID deviceId = UUID.fromString(deviceIdStr);

            String userIdStr = (String) map.get("userId");
            UUID userId = userIdStr != null ? UUID.fromString(userIdStr) : null;

            Double maxConsumption = 0.0;
            if (map.containsKey("maxConsumption") && map.get("maxConsumption") != null) {
                maxConsumption = Double.valueOf(map.get("maxConsumption").toString());
            }

            if ("CREATED".equalsIgnoreCase(action)) {
                handleDeviceCreated(deviceId,maxConsumption,userId);
            } else if("UPDATED".equalsIgnoreCase(action)) {
                handleDeviceUpdated(deviceId,maxConsumption,userId);
            }
            else if ("DELETED".equalsIgnoreCase(action)) {
                handleDeviceDeleted(deviceId);
            } else {
                LOGGER.warn("Unknown event action: {}", action);
            }

        } catch (Exception e) {
            LOGGER.error("Error processing device sync message", e);
        }
    }

    private void handleDeviceCreated(UUID deviceId,Double maxConsumption, UUID userId) {
        if (deviceMappingRepository.existsByDeviceId(deviceId)) {
            return;
        }
        deviceMappingRepository.save(new DeviceMapping(deviceId, maxConsumption, userId));
        LOGGER.info("Synced: Created device mapping for {} with limit {}", deviceId, maxConsumption);
    }

    private void handleDeviceUpdated(UUID deviceId, Double maxConsumption, UUID userId) {
        // Find existing mapping or create a new one if it somehow doesn't exist (Upsert)
        DeviceMapping mapping = deviceMappingRepository.findById(deviceId)
                .orElse(new DeviceMapping(deviceId, maxConsumption, userId));

        mapping.setMaxHourlyConsumption(maxConsumption);
        mapping.setUserId(userId);
        deviceMappingRepository.save(mapping);

        LOGGER.info("Synced: Updated device mapping for {} with limit {}", deviceId, maxConsumption);
    }

    private void handleDeviceDeleted(UUID deviceId) {
        if (deviceMappingRepository.existsById(deviceId)) {
            deviceMappingRepository.deleteById(deviceId);
        }
        hourlyConsumptionRepository.deleteByDeviceId(deviceId);
        LOGGER.info("Synced: Deleted device mapping and data for {}", deviceId);
    }
}