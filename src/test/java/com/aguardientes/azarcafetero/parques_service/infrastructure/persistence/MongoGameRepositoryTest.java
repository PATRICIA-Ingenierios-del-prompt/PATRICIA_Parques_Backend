package com.aguardientes.azarcafetero.parques_service.infrastructure.persistence;

import com.aguardientes.azarcafetero.parques_service.domain.model.Game;
import com.aguardientes.azarcafetero.parques_service.domain.model.Player;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MongoGameRepositoryTest {

    private final SpringGameDocumentRepository documents = mock(SpringGameDocumentRepository.class);
    private final MongoGameRepository repository = new MongoGameRepository(documents);

    private Game newGame() {
        return new Game("g1", List.of(
                new Player("p1", "Ana", "AMARILLO", 4),
                new Player("p2", "Beto", "AZUL", 21)));
    }

    @Test
    void saveWritesMappedDocument() {
        repository.save(newGame());

        ArgumentCaptor<GameDocument> captor = ArgumentCaptor.forClass(GameDocument.class);
        verify(documents).save(captor.capture());
        assertEquals("g1", captor.getValue().id);
        assertEquals(2, captor.getValue().players.size());
    }

    @Test
    void findByIdReturnsMappedGame() {
        when(documents.findById("g1"))
                .thenReturn(Optional.of(GameDocumentMapper.toDocument(newGame())));

        Game found = repository.findById("g1");
        assertEquals("g1", found.getId());
        assertEquals("Ana", found.getPlayers().get(0).getName());
    }

    @Test
    void findByIdThrowsSameContractAsInMemoryWhenMissing() {
        when(documents.findById("nope")).thenReturn(Optional.empty());

        // El join por WebSocket depende de ESTE tipo y mensaje para crear on-demand.
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> repository.findById("nope"));
        assertEquals("Juego no encontrado: nope", ex.getMessage());
    }
}
