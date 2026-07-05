package com.aguardientes.azarcafetero.parques_service.infrastructure.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final ParquesRabbitProperties rabbitProperties;

    public WebSocketConfig(ParquesRabbitProperties rabbitProperties) {
        this.rabbitProperties = rabbitProperties;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        var brokerRelay = config.enableStompBrokerRelay("/exchange");
        brokerRelay.setRelayHost(rabbitProperties.relayHost());
        brokerRelay.setRelayPort(rabbitProperties.relayPort());
        brokerRelay.setClientLogin(rabbitProperties.clientLogin());
        brokerRelay.setClientPasscode(rabbitProperties.clientPasscode());
        brokerRelay.setSystemLogin(rabbitProperties.systemLogin());
        brokerRelay.setSystemPasscode(rabbitProperties.systemPasscode());
        brokerRelay.setVirtualHost(rabbitProperties.virtualHost());
        brokerRelay.setAutoStartup(rabbitProperties.autoStartup());

        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/parques-ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }
}
