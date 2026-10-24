package com.Santhosh1802.ProjectManagement.realtime.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Arrays;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${cors.allowed-origins:http://localhost:4321,https://devflow-km.vercel.app}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable in-memory simple broker for destinations with /queue and /topic prefixes
        config.enableSimpleBroker("/queue", "/topic");
        // Prefix for client-to-server messages
        config.setApplicationDestinationPrefixes("/app");
        // Destination prefix for user-specific queues
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        // Standard WebSocket endpoint with SockJS fallback support
        registry.addEndpoint("/ws")
                .setAllowedOrigins(origins)
                .withSockJS();

        // Direct pure WebSocket endpoint for native STOMP clients
        registry.addEndpoint("/ws")
                .setAllowedOrigins(origins);
    }
}
