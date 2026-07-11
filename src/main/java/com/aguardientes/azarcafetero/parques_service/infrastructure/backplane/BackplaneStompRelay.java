package com.aguardientes.azarcafetero.parques_service.infrastructure.backplane;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.nio.charset.StandardCharsets;

/**
 * Lado receptor del backplane: cada pod esta suscrito al canal del MS y, al
 * llegar un mensaje, lo reenvia a su broker STOMP local.
 */
public class BackplaneStompRelay implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(BackplaneStompRelay.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public BackplaneStompRelay(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            BackplaneEnvelope envelope = objectMapper.readValue(json, BackplaneEnvelope.class);
            // Nota: un "payload": null en el JSON llega como NullNode, no como null Java.
            if (envelope.destination() == null || envelope.payload() == null || envelope.payload().isNull()) {
                log.warn("Backplane message dropped: missing destination or payload");
                return;
            }
            messagingTemplate.convertAndSend(envelope.destination(), envelope.payload());
        } catch (Exception ex) {
            // Nunca tumbar el listener container: un mensaje malformado se descarta.
            log.warn("Failed to relay backplane message: {}", ex.getMessage());
        }
    }
}
