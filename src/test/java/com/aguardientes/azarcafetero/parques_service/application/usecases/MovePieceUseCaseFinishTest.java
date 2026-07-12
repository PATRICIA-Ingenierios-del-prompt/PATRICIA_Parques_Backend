package com.aguardientes.azarcafetero.parques_service.application.usecases;

import com.aguardientes.azarcafetero.parques_service.domain.events.GameFinishedEvent;
import com.aguardientes.azarcafetero.parques_service.domain.model.Game;
import com.aguardientes.azarcafetero.parques_service.domain.model.GameState;
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
 * Tests que ACTUALMENTE hacen que el juego termine (a diferencia de
 * MovePieceUseCaseExtendedTest cuyas aserciones estaban dentro de un
 * if (game.isFinished()) que no siempre se cumplía).
 *
 * Aquí preparamos 3 fichas ya en victoria (rel=70) y la 4ta en rel=69.
 * Al moverla con die=1 -> llega a 70 -> todas en victoria -> hasFinished=true
 * -> state=FINISHED -> se publica GameFinishedEvent.
 */
@ExtendWith(MockitoExtension.class)
class MovePieceUseCaseFinishTest {

    @Mock private GameRepository repository;
    @Mock private EventPublisher eventPublisher;

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

    private void prepareNearVictory(Player player) {
        List<Piece> pieces = player.getPieces();
        for (int i = 0; i < 3; i++) {
            pieces.get(i).exitJail();
            pieces.get(i).move(70);
        }
        pieces.get(3).exitJail();
        pieces.get(3).move(69);
    }

    @Test
    void execute_whenGameFinishes_setsFinishedStateAndPublishesEvent() throws Exception {
        Player winner = new Player("w1", "W", "AMARILLO", 4);
        Player loser  = new Player("l1", "L", "AZUL", 21);
        Game game = new Game("game-evt", List.of(winner, loser));
        game.start();

        prepareNearVictory(winner);
        injectDice(game, 1, 2);
        when(repository.findById("game-evt")).thenReturn(game);

        Game result = useCase.execute("game-evt", "w1", winner.getPieces().get(3).getId(), 1);

        assertTrue(result.isFinished());
        assertEquals("w1", result.getWinnerId());
        assertEquals(GameState.FINISHED, result.getState());

        verify(eventPublisher, atLeast(2)).publish(any());
        verify(eventPublisher).publish(any(GameFinishedEvent.class));
    }
}
