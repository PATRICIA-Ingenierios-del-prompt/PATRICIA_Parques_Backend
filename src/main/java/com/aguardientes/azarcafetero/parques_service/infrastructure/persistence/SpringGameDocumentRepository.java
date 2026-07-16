package com.aguardientes.azarcafetero.parques_service.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringGameDocumentRepository extends MongoRepository<GameDocument, String> {
}
