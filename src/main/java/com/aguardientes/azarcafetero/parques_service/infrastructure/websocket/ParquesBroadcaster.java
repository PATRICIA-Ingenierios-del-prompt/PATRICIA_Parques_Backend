package com.aguardientes.azarcafetero.parques_service.infrastructure.websocket;

import com.aguardientes.azarcafetero.parques_service.infrastructure.backplane.RedisBackplanePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;


@Component
public class ParquesBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(ParquesBroadcaster.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectProvider<RedisBackplanePublisher> backplane;

    public ParquesBroadcaster(SimpMessagingTemplate messagingTemplate,
                              ObjectProvider<RedisBackplanePublisher> backplane) {
        this.messagingTemplate = messagingTemplate;
        this.backplane = backplane;
    }

    public void send(String destination, Object payload) {
        try {
            RedisBackplanePublisher publisher = backplane.getIfAvailable();
            if (publisher != null) {
                publisher.publish(destination, payload);
            } else {
                messagingTemplate.convertAndSend(destination, payload);
            }
        } catch (RuntimeException ex) {
            log.warn("Backplane publish failed for {} -- falling back to local broadcast: {}",
                    destination, ex.getMessage());
            tryLocal(destination, payload);
        }
    }

    private void tryLocal(String destination, Object payload) {
        try {
            messagingTemplate.convertAndSend(destination, payload);
        } catch (RuntimeException ex) {
            log.warn("Local broadcast also failed for {}: {}", destination, ex.getMessage());
        }
    }
}
