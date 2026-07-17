package com.aguardientes.azarcafetero.parques_service.infrastructure.websocket;

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
        config.enableSimpleBroker("/exchange", "/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Raw STOMP-over-WebSocket for native clients (React Native / mobile),
        // matching the chat/location/board sockets which the mobile app already
        // uses successfully. SockJS framing is not raw STOMP, so a native
        // WebSocket cannot speak to a SockJS-only endpoint.
        registry.addEndpoint("/parques-ws")
                .setAllowedOriginPatterns("*");

        // SockJS transport (with fallbacks) for browser clients.
        registry.addEndpoint("/parques-ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
