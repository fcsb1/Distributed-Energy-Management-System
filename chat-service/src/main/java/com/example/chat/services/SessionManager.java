package com.example.chat.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionManager.class);

    // Map: sessionId -> WebSocketSession
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // Map: sessionId -> userType ("client" or "admin")
    private final Map<String, String> userTypes = new ConcurrentHashMap<>();

    // Map: sessionId -> username
    private final Map<String, String> usernames = new ConcurrentHashMap<>();

    // Map: clientSessionId -> adminSessionId (active chat connections)
    private final Map<String, String> clientToAdmin = new ConcurrentHashMap<>();

    public void addSession(String sessionId, WebSocketSession session, String userType, String username) {
        sessions.put(sessionId, session);
        userTypes.put(sessionId, userType);
        usernames.put(sessionId, username);
        LOGGER.info("Added {} session: {} ({})", userType, sessionId, username);
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        userTypes.remove(sessionId);
        usernames.remove(sessionId);
        clientToAdmin.remove(sessionId);
        // Also remove if this session was an admin for some client
        clientToAdmin.entrySet().removeIf(entry -> entry.getValue().equals(sessionId));
        LOGGER.info("Removed session: {}", sessionId);
    }

    public WebSocketSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public String getUserType(String sessionId) {
        return userTypes.get(sessionId);
    }

    public String getUsername(String sessionId) {
        return usernames.get(sessionId);
    }

    public List<Map<String, String>> getOnlineAdmins() {
        List<Map<String, String>> admins = new ArrayList<>();
        userTypes.forEach((sessionId, type) -> {
            if ("admin".equals(type)) {
                Map<String, String> admin = new HashMap<>();
                admin.put("sessionId", sessionId);
                admin.put("username", usernames.get(sessionId));
                admins.add(admin);
            }
        });
        return admins;
    }

    public List<Map<String, String>> getActiveClients() {
        List<Map<String, String>> clients = new ArrayList<>();
        userTypes.forEach((sessionId, type) -> {
            if ("client".equals(type)) {
                Map<String, String> client = new HashMap<>();
                client.put("sessionId", sessionId);
                client.put("username", usernames.get(sessionId));
                clients.add(client);
            }
        });
        return clients;
    }

    public void connectClientToAdmin(String clientSessionId, String adminSessionId) {
        clientToAdmin.put(clientSessionId, adminSessionId);
        LOGGER.info("Connected client {} to admin {}", clientSessionId, adminSessionId);
    }

    public String getAdminForClient(String clientSessionId) {
        return clientToAdmin.get(clientSessionId);
    }

    public Collection<WebSocketSession> getAllSessions() {
        return sessions.values();
    }

    public boolean isAdmin(String sessionId) {
        return "admin".equals(userTypes.get(sessionId));
    }
}