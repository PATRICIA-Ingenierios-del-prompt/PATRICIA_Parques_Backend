package com.aguardientes.azarcafetero.parques_service.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;


@JsonIgnoreProperties(ignoreUnknown = true)
public record ParcheCreatedEvent(UUID parcheId) {
}
