package com.aguardientes.azarcafetero.parques_service.infrastructure.persistence;

import com.aguardientes.azarcafetero.parques_service.domain.model.Game;
import com.aguardientes.azarcafetero.parques_service.domain.model.GameState;
import com.aguardientes.azarcafetero.parques_service.domain.model.Piece;
import com.aguardientes.azarcafetero.parques_service.domain.model.Player;

import java.util.List;

/** Dominio ↔ documento Mongo. Sin estado; funciones puras. */
public final class GameDocumentMapper {

    private GameDocumentMapper() {}

    public static GameDocument toDocument(Game game) {
        GameDocument doc = new GameDocument();
        doc.id = game.getId();
        doc.currentTurn = game.getCurrentTurn();
        doc.die1 = game.getDie1();
        doc.die2 = game.getDie2();
        doc.moveValue = game.getMoveValue();
        doc.jailExitAvailable = game.isJailExitAvailable();
        doc.diceRolled = game.isDiceRolled();
        doc.die1Used = game.isDie1Used();
        doc.die2Used = game.isDie2Used();
        doc.state = game.getState().name();
        doc.winnerId = game.getWinnerId();
        doc.players = game.getPlayers().stream().map(GameDocumentMapper::toDocument).toList();
        return doc;
    }

    private static GameDocument.PlayerDocument toDocument(Player player) {
        GameDocument.PlayerDocument doc = new GameDocument.PlayerDocument();
        doc.id = player.getId();
        doc.name = player.getName();
        doc.color = player.getColor();
        doc.exitPosition = player.getExitPosition();
        doc.jailAttempts = player.getJailAttempts();
        doc.consecutivePairs = player.getConsecutivePairs();
        doc.pieces = player.getPieces().stream().map(p -> {
            GameDocument.PieceDocument pd = new GameDocument.PieceDocument();
            pd.id = p.getId();
            pd.relativePosition = p.getRelativePosition();
            return pd;
        }).toList();
        return doc;
    }

    public static Game toDomain(GameDocument doc) {
        List<Player> players = doc.players.stream().map(GameDocumentMapper::toDomain).toList();
        return Game.restore(
                doc.id, players, doc.currentTurn,
                doc.die1, doc.die2, doc.moveValue,
                doc.jailExitAvailable, doc.diceRolled,
                doc.die1Used, doc.die2Used,
                GameState.valueOf(doc.state), doc.winnerId);
    }

    private static Player toDomain(GameDocument.PlayerDocument doc) {
        List<Piece> pieces = doc.pieces.stream()
                .map(p -> Piece.restore(p.id, doc.color, doc.exitPosition, p.relativePosition))
                .toList();
        return Player.restore(doc.id, doc.name, doc.color, doc.exitPosition,
                pieces, doc.jailAttempts, doc.consecutivePairs);
    }
}
