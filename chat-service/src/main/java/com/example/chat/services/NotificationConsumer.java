package com.example.chat.services;

import com.example.chat.dtos.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

@Service
public class NotificationConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationConsumer.class);
    private final SessionManager sessionManager; // Use SessionManager directly
    private final ObjectMapper objectMapper;

    public NotificationConsumer(SessionManager sessionManager, ObjectMapper objectMapper) {
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "overconsumption.notifications")
    public void receiveNotification(String messageJson) {
        try {
            LOGGER.info("Received raw notification from RabbitMQ: {}", messageJson);

            // Parse the JSON
            Map<String, Object> payload = objectMapper.readValue(messageJson, Map.class);
            String deviceId = (String) payload.get("deviceId");
            String userId = (String) payload.get("userId"); // Extract the target user ID
            Double current = Double.valueOf(payload.get("current").toString());
            Double max = Double.valueOf(payload.get("max").toString());

            String notificationText = String.format(
                    "Warning: Device %s exceeded max consumption! Current: %.2f kWh, Max: %.2f kWh",
                    deviceId, current, max);

            // Logic to send to SPECIFIC user
            if (userId != null && !"null".equals(userId)) {
                WebSocketSession session = sessionManager.getSessionByUserId(userId);

                if (session != null && session.isOpen()) {
                    // Create notification message
                    ChatMessage notification = new ChatMessage("notification", "system", "user", notificationText);
                    String json = objectMapper.writeValueAsString(notification);

                    // Send
                    session.sendMessage(new TextMessage(json));
                    LOGGER.info("Sent overconsumption notification to User ID: {}", userId);
                } else {
                    LOGGER.warn("User ID {} is not currently connected via WebSocket. Notification dropped.", userId);
                }
            } else {
                LOGGER.warn("Notification received with no valid User ID. Cannot route.");
            }

        } catch (Exception e) {
            LOGGER.error("Error processing overconsumption notification", e);
        }
    }
}