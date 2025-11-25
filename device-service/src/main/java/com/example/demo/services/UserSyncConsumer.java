package com.example.demo.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class UserSyncConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSyncConsumer.class);
    private final DeviceService deviceService;
    private final ObjectMapper objectMapper;

    public UserSyncConsumer(DeviceService deviceService, ObjectMapper objectMapper) {
        this.deviceService = deviceService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "device.user.sync.queue")
    public void handleSyncMessage(String messageJson) {
        try {
            // 1. String to Map
            Map<String, Object> map = objectMapper.readValue(messageJson, Map.class);

            // 2. Extract values
            String action = (String) map.get("action");
            String userIdStr = (String) map.get("userId");
            UUID userId = UUID.fromString(userIdStr);

            LOGGER.info("Device Service received user action: {}", action);

            if ("CREATED".equals(action)) {
                deviceService.createUserMapping(userId);
            } else if ("DELETED".equals(action)) {
                deviceService.deleteUserMapping(userId);
            }

        } catch (Exception e) {
            LOGGER.error("Error processing user sync in Device Service", e);
        }
    }
}