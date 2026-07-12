package com.aguardientes.azarcafetero.parques_service.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitProvisioningConfig {
    public static final String PARCHE_EVENTS_EXCHANGE = "parche.events";

    public static final String PARCHE_CREATED_ROUTING_KEY  = "parche.created";
    public static final String PARQUES_READY_ROUTING_KEY   = "parche.parques.ready";

    public static final String PARCHE_CREATED_QUEUE = "parques.parche.created.queue";

    @Bean
    public TopicExchange parcheEventsExchange() {
        // Ya lo creo Parches como durable/non-auto-delete; aca solo lo declaramos
        // para poder bindear la cola. Si Parches lo definio distinto, RabbitMQ
        // rechaza la redeclaracion -- estos flags deben coincidir con Parches.
        return new TopicExchange(PARCHE_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue parcheCreatedQueue() {
        return QueueBuilder.durable(PARCHE_CREATED_QUEUE).build();
    }

    @Bean
    public Binding parcheCreatedBinding() {
        return BindingBuilder
                .bind(parcheCreatedQueue())
                .to(parcheEventsExchange())
                .with(PARCHE_CREATED_ROUTING_KEY);
    }

    /**
     * Converter para AMQP puro. Parches usa TypePrecedence.INFERRED, asi que
     * NO necesita el header __TypeId__: basta con que el JSON tenga los
     * mismos campos que el DTO del receptor.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
