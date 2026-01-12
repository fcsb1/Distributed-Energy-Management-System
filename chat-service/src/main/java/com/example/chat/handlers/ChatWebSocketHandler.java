package com.example.chat.handlers;

import com.example.chat.dtos.ChatMessage;
import com.example.chat.services.GeminiChatService;
import com.example.chat.services.RuleBasedChatService;
import com.example.chat.services.SessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final SessionManager sessionManager;
    private final RuleBasedChatService ruleBasedChatService;
    private final GeminiChatService geminiChatService;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(SessionManager sessionManager,
                                RuleBasedChatService ruleBasedChatService,
                                GeminiChatService geminiChatService,
                                ObjectMapper objectMapper) {
        this.sessionManager = sessionManager;
        this.ruleBasedChatService = ruleBasedChatService;
        this.geminiChatService = geminiChatService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();

        // Extract userType, username AND userId from query params
        String query = session.getUri().getQuery();
        String userType = extractParam(query, "userType"); // "client" or "admin"
        String username = extractParam(query, "username");
        String userId = extractParam(query, "userId"); // Extract User ID

        if (userType == null || username == null) {
            LOGGER.warn("Connection rejected: missing userType or username");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        // Pass userId to the session manager
        sessionManager.addSession(sessionId, session, userType, username, userId);

        // Send welcome message
        ChatMessage welcome = new ChatMessage("system", "server", sessionId,
                "Connected as " + userType + ": " + username);
        sendMessage(session, welcome);

        // If client, send available admins list
        if ("client".equals(userType)) {
            sendAdminList(session);
        }

        // If admin, send list of active clients
        if ("admin".equals(userType)) {
            sendClientList(session);
        }

        LOGGER.info("WebSocket connection established: {} ({}) UserID: {}", sessionId, userType, userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload();

        try {
            ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);
            String type = chatMessage.getType();

            LOGGER.debug("Received message type: {} from {}", type, sessionId);

            switch (type) {
                case "chat":
                    handleChatMessage(session, chatMessage);
                    break;
                case "select_admin":
                    handleAdminSelection(session, chatMessage);
                    break;
                case "get_admins":
                    sendAdminList(session);
                    break;
                case "get_clients":
                    sendClientList(session);
                    break;
                default:
                    LOGGER.warn("Unknown message type: {}", type);
            }
        } catch (Exception e) {
            LOGGER.error("Error processing message", e);
        }
    }

    private void handleChatMessage(WebSocketSession session, ChatMessage chatMessage) throws Exception {
        String sessionId = session.getId();
        String userType = sessionManager.getUserType(sessionId);
        String message = chatMessage.getMessage();

        if ("client".equals(userType)) {
            // Check if client is connected to an admin
            String adminSessionId = sessionManager.getAdminForClient(sessionId);

            if (adminSessionId != null) {
                // Forward to admin
                WebSocketSession adminSession = sessionManager.getSession(adminSessionId);
                if (adminSession != null && adminSession.isOpen()) {
                    ChatMessage toAdmin = new ChatMessage("chat",
                            sessionManager.getUsername(sessionId),
                            adminSessionId,
                            message);
                    toAdmin.setAdminId(sessionId); // So admin knows which client
                    sendMessage(adminSession, toAdmin);
                }
            } else {
                // Chat with bot
                handleBotChat(session, message);
            }
        } else if ("admin".equals(userType)) {
            // Admin sending to client
            String targetClientId = chatMessage.getTo();
            WebSocketSession clientSession = sessionManager.getSession(targetClientId);

            if (clientSession != null && clientSession.isOpen()) {
                ChatMessage toClient = new ChatMessage("chat",
                        "Admin " + sessionManager.getUsername(sessionId),
                        targetClientId,
                        message);
                sendMessage(clientSession, toClient);
            }
        }
    }

    private void handleBotChat(WebSocketSession session, String userMessage) throws Exception {
        // Try rule-based first
        String response = ruleBasedChatService.getResponse(userMessage);

        if (response == null) {
            // Fallback to Gemini AI
            response = geminiChatService.getResponse(userMessage);
        }

        ChatMessage botResponse = new ChatMessage("chat", "Bot", session.getId(), response);
        sendMessage(session, botResponse);
    }

    private void handleAdminSelection(WebSocketSession session, ChatMessage chatMessage) throws Exception {
        String adminSessionId = chatMessage.getAdminId();
        sessionManager.connectClientToAdmin(session.getId(), adminSessionId);

        ChatMessage confirmation = new ChatMessage("system", "server", session.getId(),
                "Connected to admin. You can now chat.");
        sendMessage(session, confirmation);

        // Notify admin
        WebSocketSession adminSession = sessionManager.getSession(adminSessionId);
        if (adminSession != null && adminSession.isOpen()) {
            ChatMessage notification = new ChatMessage("system", "server", adminSessionId,
                    "Client " + sessionManager.getUsername(session.getId()) + " connected to you");
            sendMessage(adminSession, notification);
        }
    }

    private void sendAdminList(WebSocketSession session) throws Exception {
        List<Map<String, String>> admins = sessionManager.getOnlineAdmins();
        ChatMessage adminListMsg = new ChatMessage("admin_list", "server", session.getId(),
                objectMapper.writeValueAsString(admins));
        sendMessage(session, adminListMsg);
    }

    private void sendClientList(WebSocketSession session) throws Exception {
        List<Map<String, String>> clients = sessionManager.getActiveClients();
        ChatMessage clientListMsg = new ChatMessage("client_list", "server", session.getId(),
                objectMapper.writeValueAsString(clients));
        sendMessage(session, clientListMsg);
    }

    private void sendMessage(WebSocketSession session, ChatMessage message) throws Exception {
        if (session.isOpen()) {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionManager.removeSession(session.getId());
        LOGGER.info("WebSocket connection closed: {}", session.getId());
    }

    private String extractParam(String query, String param) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2 && kv[0].equals(param)) {
                return kv[1];
            }
        }
        return null;
    }
}