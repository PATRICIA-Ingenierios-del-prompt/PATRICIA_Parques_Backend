package com.aguardientes.azarcafetero.parques_service.application.usecases;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reserva un {@code parquesId} por cada parche. Idempotente: si el mismo
 * {@code parcheId} llega varias veces (RabbitMQ es at-least-once), devuelve
 * el MISMO parquesId ya reservado -- asi Parches nunca ve dos IDs distintos
 * para el mismo parche.
 *
 * NO crea un Game todavia: {@link CreateGameUseCase} exige >=1 jugador y en
 * `parche.created` no hay jugadores. El Game se crea cuando alguien inicia
 * la partida por WebSocket usando ESTE parquesId como gameId. Es decir,
 * `parquesId` es tambien el `gameId` reservado para el parche.
 *
 * Persistencia en memoria: encaja con el estilo actual del MS
 * ({@link com.aguardientes.azarcafetero.parques_service.infrastructure.InMemoryGameRepository}).
 * Si el pod se reinicia, la reserva se pierde; para produccion, mover a
 * Redis o Postgres.
 */
@Service
public class ProvisionParquesForParcheUseCase {

    private final Map<UUID, UUID> parcheIdToParquesId = new ConcurrentHashMap<>();

    /** @return el parquesId (nuevo o el previamente reservado para ese parche). */
    public UUID provision(UUID parcheId) {
        return parcheIdToParquesId.computeIfAbsent(parcheId, id -> UUID.randomUUID());
    }
}
