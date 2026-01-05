package com.example.chat.dtos;

public class ChatMessage {
    private String type; // "chat", "notification", "admin_list", "system"
    private String from; // userId or "bot" or "admin"
    private String to; // userId or "all"
    private String message;
    private String timestamp;
    private String adminId; // when selecting admin

    public ChatMessage() {
    }

    public ChatMessage(String type, String from, String to, String message) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.message = message;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
}