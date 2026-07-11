package com.aguardientes.azarcafetero.parques_service.infrastructure.backplane;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Cableado del backplane (activo solo con backplane.enabled=true).
 *
 * Parques NO usa otro Redis (a diferencia de Location con cache), asi que NO
 * hace falta el truco @Primary sobre el ConnectionFactory auto-configurado.
 */
@Configuration
@ConditionalOnProperty(prefix = "backplane", name = "enabled", havingValue = "true")
public class BackplaneConfig {

    @Bean
    public LettuceConnectionFactory backplaneConnectionFactory(BackplaneProperties props) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(
                props.getRedis().getHost(), props.getRedis().getPort());
        if (props.getRedis().getPassword() != null && !props.getRedis().getPassword().isBlank()) {
            config.setPassword(props.getRedis().getPassword());
        }
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public StringRedisTemplate backplaneRedisTemplate(
            @Qualifier("backplaneConnectionFactory") RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    @Bean
    public RedisBackplanePublisher redisBackplanePublisher(
            @Qualifier("backplaneRedisTemplate") StringRedisTemplate backplaneRedisTemplate,
            ObjectMapper objectMapper,
            BackplaneProperties props) {
        return new RedisBackplanePublisher(backplaneRedisTemplate, objectMapper, props.getChannel());
    }

    @Bean
    public BackplaneStompRelay backplaneStompRelay(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        return new BackplaneStompRelay(messagingTemplate, objectMapper);
    }

    @Bean
    public RedisMessageListenerContainer backplaneListenerContainer(
            @Qualifier("backplaneConnectionFactory") RedisConnectionFactory factory,
            BackplaneStompRelay relay,
            BackplaneProperties props) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(relay, new ChannelTopic(props.getChannel()));
        return container;
    }
}
