package com.example.koku.ai;

import com.example.koku.domain.Move;

import java.util.Objects;
import java.util.Optional;

public record BotMoveResult(
        Optional<Move> move,
        BotDifficulty difficulty,
        String explanation
) {
    public BotMoveResult {
        Objects.requireNonNull(move, "move cannot be null");
        Objects.requireNonNull(difficulty, "difficulty cannot be null");
    }

    public static BotMoveResult of(Move move, BotDifficulty difficulty) {
        return new BotMoveResult(
                Optional.of(Objects.requireNonNull(move, "move cannot be null")),
                difficulty,
                ""
        );
    }

    public static BotMoveResult noMove(BotDifficulty difficulty) {
        return new BotMoveResult(Optional.empty(), difficulty, "No legal moves available.");
    }
}
