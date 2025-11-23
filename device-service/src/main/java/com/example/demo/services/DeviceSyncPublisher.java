package com.example.demo.services;

import com.example.demo.dtos.DeviceSyncMessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service responsible for publishing device synchronization events to RabbitMQ.
 * These messages are consumed by monitoring-service to keep device mappings in sync.
 */
@Service
public class DeviceSyncPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceSyncPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.queue.device-sync}")
    private String deviceSyncQueue;

    public DeviceSyncPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Publish a device CREATED event.
     * Called when a new device is inserted into the database.
     */
    public void publishDeviceCreated(UUID deviceId) {
        try {
            DeviceSyncMessageDTO message = DeviceSyncMessageDTO.created(deviceId);
            String jsonMessage = objectMapper.writeValueAsString(message);

            rabbitTemplate.convertAndSend(deviceSyncQueue, jsonMessage);
            LOGGER.info("Published CREATED event for device: {}", deviceId);

        } catch (Exception e) {
            LOGGER.error("Failed to publish CREATED event for device: {}", deviceId, e);
            // Don't throw exception - sync is secondary operation
        }
    }

    /**
     * Publish a device DELETED event.
     * Called when a device is deleted from the database.
     */
    public void publishDeviceDeleted(UUID deviceId) {
        try {
            DeviceSyncMessageDTO message = DeviceSyncMessageDTO.deleted(deviceId);
            String jsonMessage = objectMapper.writeValueAsString(message);

            rabbitTemplate.convertAndSend(deviceSyncQueue, jsonMessage);
            LOGGER.info("Published DELETED event for device: {}", deviceId);

        } catch (Exception e) {
            LOGGER.error("Failed to publish DELETED event for device: {}", deviceId, e);
            // Don't throw exception - sync is secondary operation
        }
    }
}