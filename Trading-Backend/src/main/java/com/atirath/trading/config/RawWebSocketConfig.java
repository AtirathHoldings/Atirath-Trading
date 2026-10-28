package com.atirath.trading.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class RawWebSocketConfig implements WebSocketConfigurer {

    private final MarketRawWebSocketHandler rawWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Direct JSON WebSocket endpoint for Mobile Flutter App
        registry.addHandler(rawWebSocketHandler, "/ws-market", "/ws-trading-live")
                .setAllowedOriginPatterns("*");
    }
}
