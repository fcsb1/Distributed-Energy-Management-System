package com.example.demo.services;

import com.example.demo.dtos.RegisterDTO;
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
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public UserSyncConsumer(AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "auth.user.sync.queue")
    public void handleSyncMessage(String messageJson) {
        try {
            // 1. Manually convert String to Map
            Map<String, Object> map = objectMapper.readValue(messageJson, Map.class);

            // 2. Extract values manually
            String action = (String) map.get("action");
            String userIdStr = (String) map.get("userId");
            String username = (String) map.get("username");
            String password = (String) map.get("password");
            String role = (String) map.get("role");

            UUID userId = UUID.fromString(userIdStr);

            LOGGER.info("Auth Service received action: {}", action);

            switch (action) {
                case "CREATED":
                    RegisterDTO dto = new RegisterDTO();
                    dto.setUserId(userId);
                    dto.setUsername(username);
                    dto.setPassword(password);
                    dto.setRole(role);
                    authService.register(dto);
                    break;
                case "UPDATED":
                    RegisterDTO updateDto = new RegisterDTO();
                    updateDto.setUserId(userId);
                    updateDto.setUsername(username);
                    updateDto.setPassword(password);
                    updateDto.setRole(role);
                    authService.updateCredential(userId, updateDto);
                    break;
                case "DELETED":
                    authService.deleteCredential(userId);
                    break;
                default:
                    LOGGER.warn("Unknown action: {}", action);
            }
        } catch (Exception e) {
            LOGGER.error("Error processing user sync in Auth Service", e);
        }
    }
}