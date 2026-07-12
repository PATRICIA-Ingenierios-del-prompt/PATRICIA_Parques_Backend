package com.aguardientes.azarcafetero.parques_service.infrastructure.backplane;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Mensaje que viaja por el canal Redis del backplane. Contrato COMPARTIDO
 * con los demas MS de sockets: {destination, payload}. Ver
 * PATRICIA_Backplane_Spec.txt.
 */
public record BackplaneEnvelope(String destination, JsonNode payload) {
}
