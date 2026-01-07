package com.example.demo.services;

import com.example.demo.config.RabbitConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class DeviceSyncPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceSyncPublisher.class);
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public DeviceSyncPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishDeviceEvent(String action, UUID deviceId,Double maxConsumption, UUID userId) {
        try {
            // Create Map payload
            Map<String, Object> map = new HashMap<>();
            map.put("action", action); // "CREATED" or "DELETED"
            map.put("deviceId", deviceId.toString());
            map.put("maxConsumption", maxConsumption);
            map.put("userId", userId != null ? userId.toString() : null);

            String jsonMessage = objectMapper.writeValueAsString(map);

            // Routing Key: device.event.[action]
            String routingKey = "device.event." + action.toLowerCase();

            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, routingKey, jsonMessage);

            LOGGER.info("Published device event: {} with maxConsumption: {}", routingKey, maxConsumption);
        } catch (Exception e) {
            LOGGER.error("Failed to publish device event", e);
        }
    }
}