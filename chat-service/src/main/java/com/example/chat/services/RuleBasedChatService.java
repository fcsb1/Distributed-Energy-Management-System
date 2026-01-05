package com.example.chat.services;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RuleBasedChatService {

    private final Map<String, String> rules = new HashMap<>();

    public RuleBasedChatService() {
        initializeRules();
    }

    private void initializeRules() {
        // Simple keyword-based rules (10+ rules)
        rules.put("hello", "Hello! How can I help you today?");
        rules.put("hi", "Hi there! What can I do for you?");
        rules.put("help", "I can help you with device management, energy monitoring, and account information. What would you like to know?");
        rules.put("device", "You can view your devices in the 'My Devices' section. Each device shows its maximum consumption limit.");
        rules.put("consumption", "Energy consumption is measured in kWh. You can view hourly consumption charts for each device.");
        rules.put("account", "To manage your account, contact an administrator through this chat.");
        rules.put("password", "To change your password, please contact an administrator.");
        rules.put("admin", "Would you like to speak with an administrator? I can connect you to one.");
        rules.put("thank", "You're welcome! Is there anything else I can help you with?");
        rules.put("bye", "Goodbye! Feel free to return if you have more questions.");
        rules.put("how", "I'm here to assist you. Please ask me about devices, energy consumption, or your account.");
        rules.put("what", "I can provide information about the Energy Management System. What would you like to know?");
        rules.put("monitor", "The monitoring system tracks your device energy consumption in real-time and stores hourly data.");
        rules.put("alert", "You'll receive alerts when a device exceeds its maximum consumption limit.");
        rules.put("chart", "Energy charts show hourly consumption patterns for each device. Select a device and date to view the chart.");
    }

    public String getResponse(String message) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }

        String lowerMessage = message.toLowerCase().trim();

        // Check each rule
        for (Map.Entry<String, String> rule : rules.entrySet()) {
            if (lowerMessage.contains(rule.getKey())) {
                return rule.getValue();
            }
        }

        // No rule matched
        return null;
    }
}