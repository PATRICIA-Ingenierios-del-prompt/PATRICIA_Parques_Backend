package com.aguardientes.azarcafetero.parques_service.infrastructure.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Snapshot Mongo de una partida. Es un espejo plano del dominio (Game/Player/
 * Piece) para que CUALQUIER pod pueda recargar el estado: sin esto las
 * partidas vivían en el InMemoryGameRepository de un solo pod y el segundo
 * jugador (enrutado a otro pod) veía una partida distinta con el mismo id.
 */
@Document("games")
public class GameDocument {

    @Id
    public String id;
    public int currentTurn;
    public int die1;
    public int die2;
    public int moveValue;
    public boolean jailExitAvailable;
    public boolean diceRolled;
    public boolean die1Used;
    public boolean die2Used;
    public String state;
    public String winnerId;
    public List<PlayerDocument> players;

    public static class PlayerDocument {
        public String id;
        public String name;
        public String color;
        public int exitPosition;
        public int jailAttempts;
        public int consecutivePairs;
        public List<PieceDocument> pieces;
    }

    public static class PieceDocument {
        public String id;
        public int relativePosition;
    }
}
