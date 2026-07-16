package com.aguardientes.azarcafetero.parques_service.infrastructure.persistence;

import com.aguardientes.azarcafetero.parques_service.domain.model.Game;
import com.aguardientes.azarcafetero.parques_service.domain.model.GameState;
import com.aguardientes.azarcafetero.parques_service.domain.model.Piece;
import com.aguardientes.azarcafetero.parques_service.domain.model.Player;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameDocumentMapperTest {

    private Game gameInFlight() {
        // Partida a mitad de juego: fichas en cárcel, en el anillo y en victoria.
        Piece jailed  = Piece.restore("p1-piece-0", "AMARILLO", 4, -1);
        Piece onTrack = Piece.restore("p1-piece-1", "AMARILLO", 4, 12);
        Piece ladder  = Piece.restore("p1-piece-2", "AMARILLO", 4, 65);
        Piece atHome  = Piece.restore("p1-piece-3", "AMARILLO", 4, 70);
        Player p1 = Player.restore("p1", "Ana", "AMARILLO", 4,
                List.of(jailed, onTrack, ladder, atHome), 1, 2);
        Player p2 = new Player("p2", "Bot", "AZUL", 21);

        return Game.restore("game-42", List.of(p1, p2), 1,
                3, 5, 8, true, true, false, true,
                GameState.IN_PROGRESS, null);
    }

    @Test
    void roundTripPreservesFullGameState() {
        Game original = gameInFlight();

        Game restored = GameDocumentMapper.toDomain(GameDocumentMapper.toDocument(original));

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getCurrentTurn(), restored.getCurrentTurn());
        assertEquals(original.getDie1(), restored.getDie1());
        assertEquals(original.getDie2(), restored.getDie2());
        assertEquals(original.getMoveValue(), restored.getMoveValue());
        assertEquals(original.isJailExitAvailable(), restored.isJailExitAvailable());
        assertEquals(original.isDiceRolled(), restored.isDiceRolled());
        assertEquals(original.isDie1Used(), restored.isDie1Used());
        assertEquals(original.isDie2Used(), restored.isDie2Used());
        assertEquals(original.getState(), restored.getState());
        assertEquals(original.getWinnerId(), restored.getWinnerId());

        assertEquals(2, restored.getPlayers().size());
        Player rp1 = restored.getPlayers().get(0);
        assertEquals("Ana", rp1.getName());
        assertEquals("AMARILLO", rp1.getColor());
        assertEquals(4, rp1.getExitPosition());
        assertEquals(1, rp1.getJailAttempts());
        assertEquals(2, rp1.getConsecutivePairs());

        List<Piece> pieces = rp1.getPieces();
        assertEquals(4, pieces.size());
        assertTrue(pieces.get(0).isInJail());
        assertEquals(12, pieces.get(1).getRelativePosition());
        assertTrue(pieces.get(2).isOnLadder());
        assertTrue(pieces.get(3).isAtVictory());
        // La posición absoluta se deriva de color+exit → debe sobrevivir el viaje
        assertEquals(pieces.get(1).getAbsolutePosition(),
                gameInFlight().getPlayers().get(0).getPieces().get(1).getAbsolutePosition());
    }

    @Test
    void restoredGameKeepsPlayingNormally() {
        Game restored = GameDocumentMapper.toDomain(GameDocumentMapper.toDocument(gameInFlight()));
        // Turno 1 = p2; el dado 2 (5) sigue disponible: p2 puede tirar en su momento,
        // y validar el turno del jugador equivocado debe fallar como siempre.
        assertEquals("p2", restored.getCurrentPlayer().getId());
        assertThrows(IllegalStateException.class, () -> restored.rollDice("p1"));
    }

    @Test
    void finishedGameRoundTripsWinner() {
        Game finished = Game.restore("g2", List.of(new Player("p1", "Ana", "AMARILLO", 4),
                        new Player("p2", "Beto", "AZUL", 21)),
                0, 6, 6, 12, false, false, true, true,
                GameState.FINISHED, "p1");
        Game restored = GameDocumentMapper.toDomain(GameDocumentMapper.toDocument(finished));
        assertTrue(restored.isFinished());
        assertEquals("p1", restored.getWinnerId());
    }
}
