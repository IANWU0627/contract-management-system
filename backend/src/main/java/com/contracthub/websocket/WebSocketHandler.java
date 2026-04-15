package com.contracthub.websocket;

import com.contracthub.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtUtils jwtUtils;
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    public WebSocketHandler(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        var uri = session.getUri();
        String query = uri != null ? uri.getQuery() : null;
        Map<String, String> params = parseQuery(query);
        String token = params.get("token");

        if (token == null || token.isBlank() || !jwtUtils.validateToken(token) || jwtUtils.isTokenExpired(token)) {
            logger.warn("WebSocket rejected due to invalid token, session: {}", session.getId());
            session.close(Objects.requireNonNull(CloseStatus.POLICY_VIOLATION));
            return;
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        if (userId == null) {
            logger.warn("WebSocket rejected due to missing userId claim, session: {}", session.getId());
            session.close(Objects.requireNonNull(CloseStatus.POLICY_VIOLATION));
            return;
        }

        userSessions.put(userId, session);
        logger.info("WebSocket connected for user: {}", userId);
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
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
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        userSessions.values().remove(session);
        logger.info("WebSocket closed: {} with status: {}", session.getId(), status);
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) throws Exception {
        logger.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        userSessions.values().remove(session);
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
        String json = Objects.requireNonNull(objectMapper.writeValueAsString(message));
        session.sendMessage(new TextMessage(json));
    }

    public boolean isUserOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    public int getOnlineCount() {
        return userSessions.size();
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isBlank()) {
            return params;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0 && idx < pair.length() - 1) {
                String key = pair.substring(0, idx);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
        return params;
    }
}
