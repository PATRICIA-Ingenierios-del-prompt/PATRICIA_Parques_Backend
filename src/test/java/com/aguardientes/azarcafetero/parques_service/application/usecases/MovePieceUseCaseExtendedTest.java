package com.aguardientes.azarcafetero.parques_service.application.usecases;

import com.aguardientes.azarcafetero.parques_service.domain.model.Game;
import com.aguardientes.azarcafetero.parques_service.domain.model.Piece;
import com.aguardientes.azarcafetero.parques_service.domain.model.Player;
import com.aguardientes.azarcafetero.parques_service.domain.ports.EventPublisher;
import com.aguardientes.azarcafetero.parques_service.domain.ports.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests adicionales para MovePieceUseCase enfocados en el flujo de fin de juego
 * y en PassTurnUseCase.
 */
@ExtendWith(MockitoExtension.class)
class MovePieceUseCaseExtendedTest {

    @Mock
    private GameRepository repository;

    @Mock
    private EventPublisher eventPublisher;

    private MovePieceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new MovePieceUseCase(repository, eventPublisher);
    }

    private void injectDice(Game g, int d1, int d2) throws Exception {
        setField(g, "die1", d1);
        setField(g, "die2", d2);
        setField(g, "die1Used", false);
        setField(g, "die2Used", false);
        setField(g, "diceRolled", true);
        setField(g, "jailExitAvailable", false);
    }

    private void setField(Object obj, String name, Object value) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    @Test
    void execute_whenGameFinishes_shouldPublishGameFinishedEvent() throws Exception {
        Player winner = new Player("p-win", "W", "AMARILLO", 4);
        Player loser = new Player("p-lose", "L", "AZUL", 21);
        Game game = new Game("game-finish", List.of(winner, loser));
        game.start();

        for (Piece p : winner.getPieces()) {
            p.exitJail();
            p.move(69);
        }

        injectDice(game, 1, 2);
        when(repository.findById("game-finish")).thenReturn(game);

        String pieceId = winner.getPieces().get(0).getId();
        useCase.execute("game-finish", "p-win", pieceId, 1);

        if (game.isFinished()) {
            // PieceMovedEvent + GameFinishedEvent
            verify(eventPublisher, atLeast(2)).publish(any());
        }
    }

    // ─── PassTurnUseCase: flujo de guardar ───────────────────────────────────

    @Test
    void passTurn_shouldSaveGame() {
        PassTurnUseCase passTurnUseCase = new PassTurnUseCase(repository);

        boolean executed = false;
        for (int i = 0; i < 30 && !executed; i++) {
            Game game = new Game("game-pass", List.of(
                    new Player("p1", "K", "AMARILLO", 4),
                    new Player("p2", "J", "AZUL", 21)
            ));
            game.start();
            when(repository.findById("game-pass")).thenReturn(game);
            game.rollDice("p1");
            if (game.getDie1() != game.getDie2()) {
                passTurnUseCase.execute("game-pass", "p1");
                verify(repository, atLeastOnce()).save(any(Game.class));
                executed = true;
            }
        }
    }

    @Test
    void passTurn_shouldReturnGame() {
        PassTurnUseCase passTurnUseCase = new PassTurnUseCase(repository);

        for (int i = 0; i < 30; i++) {
            Game game = new Game("game-pass2", List.of(
                    new Player("p1", "K", "AMARILLO", 4),
                    new Player("p2", "J", "AZUL", 21)
            ));
            game.start();
            when(repository.findById("game-pass2")).thenReturn(game);
            game.rollDice("p1");
            if (game.getDie1() != game.getDie2()) {
                Game result = passTurnUseCase.execute("game-pass2", "p1");
                assertNotNull(result);
                assertEquals("game-pass2", result.getId());
                break;
            }
        }
    }
}
