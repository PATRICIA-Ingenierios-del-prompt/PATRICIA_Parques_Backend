package com.aguardientes.azarcafetero.parques_service.infrastructure.persistence;

import com.aguardientes.azarcafetero.parques_service.domain.model.Game;
import com.aguardientes.azarcafetero.parques_service.domain.ports.GameRepository;

/**
 * GameRepository respaldado por Mongo (Atlas, DB "Parques"). Mantiene el
 * contrato de error del puerto: findById lanza IllegalArgumentException con
 * el MISMO mensaje que InMemoryGameRepository — el flujo de join por WebSocket
 * depende de atrapar esa excepción para crear la partida on-demand.
 */
public class MongoGameRepository implements GameRepository {

    private final SpringGameDocumentRepository documents;

    public MongoGameRepository(SpringGameDocumentRepository documents) {
        this.documents = documents;
    }

    @Override
    public Game findById(String id) {
        return documents.findById(id)
                .map(GameDocumentMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado: " + id));
    }

    @Override
    public void save(Game game) {
        documents.save(GameDocumentMapper.toDocument(game));
    }
}
