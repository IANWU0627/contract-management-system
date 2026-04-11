package com.contracthub.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> anonymousSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null && query.contains("userId=")) {
            Long userId = Long.parseLong(query.split("=")[1]);
            userSessions.put(userId, session);
            logger.info("WebSocket connected for user: {}", userId);
        } else {
            anonymousSessions.put(session.getId(), session);
            logger.info("WebSocket connected (anonymous): {}", session.getId());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Map<String, Object> data = objectMapper.readValue(message.getPayload(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            String type = (String) data.get("type");
            
            if ("ping".equals(type)) {
                sendMessage(session, Map.of("type", "pong", "timestamp", System.currentTimeMillis()));
            }
        } catch (Exception e) {
            logger.warn("Invalid WebSocket message: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        userSessions.values().remove(session);
        anonymousSessions.remove(session.getId());
        logger.info("WebSocket closed: {} with status: {}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        userSessions.values().remove(session);
        anonymousSessions.remove(session.getId());
    }

    public void sendToUser(Long userId, Object message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                sendMessage(session, message);
                logger.debug("Message sent to user {}: {}", userId, message);
            } catch (Exception e) {
                logger.error("Failed to send message to user {}: {}", userId, e.getMessage());
            }
        } else {
            logger.debug("User {} not connected, message queued", userId);
        }
    }

    public void broadcast(Object message) {
        userSessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    sendMessage(session, message);
                } catch (Exception e) {
                    logger.error("Failed to broadcast to session {}: {}", session.getId(), e.getMessage());
                }
            }
        });
    }

    private void sendMessage(WebSocketSession session, Object message) throws IOException {
        String json = objectMapper.writeValueAsString(message);
        session.sendMessage(new TextMessage(json));
    }

    public boolean isUserOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    public int getOnlineCount() {
        return userSessions.size() + anonymousSessions.size();
    }
}
