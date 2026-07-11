package com.aguardientes.azarcafetero.parques_service.infrastructure.messaging.event;

import java.util.UUID;


public record ParquesReadyEvent(UUID parcheId, UUID parquesId) {
}
