package com.example.monitoring.services;

import com.example.monitoring.dtos.DeviceSyncDTO;
import com.example.monitoring.entities.DeviceMapping;
import com.example.monitoring.repositories.DeviceMappingRepository;
import com.example.monitoring.repositories.HourlyConsumptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeviceSyncConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceSyncConsumer.class);

    private final DeviceMappingRepository deviceMappingRepository;
    private final HourlyConsumptionRepository hourlyConsumptionRepository;
    private final ObjectMapper objectMapper;

    public DeviceSyncConsumer(DeviceMappingRepository deviceMappingRepository,
                              HourlyConsumptionRepository hourlyConsumptionRepository) {
        this.deviceMappingRepository = deviceMappingRepository;
        this.hourlyConsumptionRepository = hourlyConsumptionRepository;
        this.objectMapper = new ObjectMapper();
    }

    @RabbitListener(queues = "${app.queue.device-sync}")
    @Transactional
    public void receiveDeviceSyncMessage(String jsonMessage) {
        try {
            LOGGER.debug("Received device sync message: {}", jsonMessage);

            // Parse JSON message
            DeviceSyncDTO syncData = objectMapper.readValue(jsonMessage, DeviceSyncDTO.class);
            LOGGER.info("Parsed device sync data: {}", syncData);

            UUID deviceId = UUID.fromString(syncData.getDeviceId());

            switch (syncData.getEventType().toUpperCase()) {
                case "CREATED":
                    handleDeviceCreated(deviceId);
                    break;
                case "DELETED":
                    handleDeviceDeleted(deviceId);
                    break;
                default:
                    LOGGER.warn("Unknown event type: {}", syncData.getEventType());
            }

        } catch (Exception e) {
            LOGGER.error("Error processing device sync message: {}", jsonMessage, e);
        }
    }

    private void handleDeviceCreated(UUID deviceId) {
        if (deviceMappingRepository.existsByDeviceId(deviceId)) {
            LOGGER.warn("Device mapping already exists for deviceId: {}", deviceId);
            return;
        }

        DeviceMapping mapping = new DeviceMapping(deviceId);
        deviceMappingRepository.save(mapping);
        LOGGER.info("Created device mapping for deviceId: {}", deviceId);
    }

    private void handleDeviceDeleted(UUID deviceId) {
        // Delete device mapping
        if (deviceMappingRepository.existsById(deviceId)) {
            deviceMappingRepository.deleteById(deviceId);
            LOGGER.info("Deleted device mapping for deviceId: {}", deviceId);
        }

        // Optionally: Delete all hourly consumption data for this device
        hourlyConsumptionRepository.deleteByDeviceId(deviceId);
        LOGGER.info("Deleted hourly consumption data for deviceId: {}", deviceId);
    }
}