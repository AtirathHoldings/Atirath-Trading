package com.atirath.trading.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Topics for broadcast: /topic/market, /topic/orderbook, /queue/user-trades
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint accessible by Next.js & Flutter apps
        registry.addEndpoint("/ws-trading")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        registry.addEndpoint("/ws-trading-raw")
                .setAllowedOriginPatterns("*"); // Direct WebSocket for mobile Flutter client
    }
}
