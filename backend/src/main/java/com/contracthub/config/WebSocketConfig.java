package com.contracthub.config;

import com.contracthub.websocket.WebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Arrays;
import java.util.Objects;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;
    private final String[] allowedOrigins;

    public WebSocketConfig(
            WebSocketHandler webSocketHandler,
            @Value("${app.websocket.allowed-origins:http://localhost:3000,http://127.0.0.1:3000}") String allowedOriginsConfig
    ) {
        this.webSocketHandler = webSocketHandler;
        this.allowedOrigins = Arrays.stream(allowedOriginsConfig.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(Objects.requireNonNull(webSocketHandler), "/ws")
                .setAllowedOrigins(Objects.requireNonNull(allowedOrigins));
    }
}
