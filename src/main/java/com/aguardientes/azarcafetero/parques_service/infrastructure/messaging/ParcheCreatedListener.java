package com.aguardientes.azarcafetero.parques_service.infrastructure.messaging;

import com.aguardientes.azarcafetero.parques_service.application.usecases.ProvisionParquesForParcheUseCase;
import com.aguardientes.azarcafetero.parques_service.infrastructure.messaging.event.ParcheCreatedEvent;
import com.aguardientes.azarcafetero.parques_service.infrastructure.messaging.event.ParquesReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
public class ParcheCreatedListener {
    private static final Logger log = LoggerFactory.getLogger(ParcheCreatedListener.class);

    private final ProvisionParquesForParcheUseCase provisionUseCase;
    private final RabbitTemplate rabbitTemplate;

    public ParcheCreatedListener(ProvisionParquesForParcheUseCase provisionUseCase, RabbitTemplate rabbitTemplate) {
        this.provisionUseCase = provisionUseCase;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitProvisioningConfig.PARCHE_CREATED_QUEUE)
    public void onParcheCreated(ParcheCreatedEvent event) {
        UUID parcheId = event.parcheId();
        if (parcheId == null) {
            log.warn("parche.created recibido sin parcheId -- descartando");
            return;
        }
        UUID parquesId = provisionUseCase.provision(parcheId);
        rabbitTemplate.convertAndSend(RabbitProvisioningConfig.PARCHE_EVENTS_EXCHANGE, RabbitProvisioningConfig.PARQUES_READY_ROUTING_KEY, new ParquesReadyEvent(parcheId, parquesId));
        log.info("Provisioned parquesId={} for parcheId={}", parquesId, parcheId);
    }
}
