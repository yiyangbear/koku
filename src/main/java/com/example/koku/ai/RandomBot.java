package com.example.koku.ai;

import com.example.koku.domain.Move;
import com.example.koku.domain.Player;
import com.example.koku.domain.Position;
import com.example.koku.domain.engine.GameEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RandomBot implements Bot {
    private static final String CONNECT_FOUR_ID = "connectFour";

    private final Random random;

    public RandomBot() {
        this(new Random());
    }

    public RandomBot(Random random) {
        this.random = Objects.requireNonNull(random, "random cannot be null");
    }

    @Override
    public BotMoveResult chooseMove(BotMoveRequest request) {
        Objects.requireNonNull(request, "request cannot be null");

        List<Move> legalMoves = legalMoves(request);
        if (legalMoves.isEmpty()) {
            return BotMoveResult.noMove(BotDifficulty.EASY);
        }

        Move selectedMove = legalMoves.get(random.nextInt(legalMoves.size()));
        return BotMoveResult.of(selectedMove, BotDifficulty.EASY);
    }

    public List<Move> legalMoves(BotMoveRequest request) {
        Objects.requireNonNull(request, "request cannot be null");

        if (request.engine().isGameOver()) {
            return List.of();
        }

        if (isConnectFour(request)) {
            return connectFourMoves(request.engine(), request.botPlayer());
        }

        return emptyCellMoves(request.engine(), request.botPlayer());
    }

    private boolean isConnectFour(BotMoveRequest request) {
        return request.gameDefinitionOptional()
                .map(definition -> CONNECT_FOUR_ID.equals(definition.id()))
                .orElse(false);
    }

    private List<Move> connectFourMoves(GameEngine engine, Player player) {
        List<Move> moves = new ArrayList<>();
        for (int col = 0; col < engine.getBoardCols(); col++) {
            for (int row = engine.getBoardRows() - 1; row >= 0; row--) {
                Position position = new Position(row, col);
                if (engine.getStoneAt(position) == null) {
                    moves.add(new Move(player, position));
                    break;
                }
            }
        }
        return moves;
    }

    private List<Move> emptyCellMoves(GameEngine engine, Player player) {
        List<Move> moves = new ArrayList<>();
        for (int row = 0; row < engine.getBoardRows(); row++) {
            for (int col = 0; col < engine.getBoardCols(); col++) {
                Position position = new Position(row, col);
                if (engine.getStoneAt(position) == null) {
                    moves.add(new Move(player, position));
                }
            }
        }
        return moves;
    }
}
