package com.aguardientes.azarcafetero.parques_service.infrastructure.websocket;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "parques.rabbitmq")
public record ParquesRabbitProperties(
        String relayHost,
        int relayPort,
        String clientLogin,
        String clientPasscode,
        String systemLogin,
        String systemPasscode,
        String virtualHost,
        boolean autoStartup
) {
}
