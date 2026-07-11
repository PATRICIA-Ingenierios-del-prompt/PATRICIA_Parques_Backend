package com.aguardientes.azarcafetero.parques_service.infrastructure.backplane;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Publica broadcasts en el canal Redis del backplane. TODOS los pods
 * (incluido el que publica) reciben el mensaje via {@link BackplaneStompRelay}
 * y lo reenvian a sus sesiones STOMP locales.
 *
 * Registrado como bean en {@link BackplaneConfig} solo si backplane.enabled=true.
 */
public class RedisBackplanePublisher {

    private final StringRedisTemplate backplaneRedis;
    private final ObjectMapper objectMapper;
    private final String channel;

    public RedisBackplanePublisher(StringRedisTemplate backplaneRedis, ObjectMapper objectMapper, String channel) {
        this.backplaneRedis = backplaneRedis;
        this.objectMapper = objectMapper;
        this.channel = channel;
    }

    public void publish(String destination, Object payload) {
        try {
            BackplaneEnvelope envelope = new BackplaneEnvelope(destination, objectMapper.valueToTree(payload));
            backplaneRedis.convertAndSend(channel, objectMapper.writeValueAsString(envelope));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Backplane payload is not serializable: " + payload.getClass(), e);
        }
    }
}
