package com.example.koku.domain.engine;

import com.example.koku.domain.GameResult;
import com.example.koku.domain.GameStatus;
import com.example.koku.domain.Move;
import com.example.koku.domain.Player;
import com.example.koku.domain.Position;

import java.util.Optional;

public interface GameEngine {
    int getBoardSize();
    default int getBoardRows() {
        return getBoardSize();
    }

    default int getBoardCols() {
        return getBoardSize();
    }

    Player getCurrentPlayer();
    GameResult getResult();
    GameStatus getStatus();
    String getLatestMessage();
    Optional<Move> getLastMove();
    Player getStoneAt(Position position);
    boolean isGameOver();

    boolean makeMove(Position position);
    boolean undo();
    void reset();
    void forceTimeoutLoss(Player loser);
}
