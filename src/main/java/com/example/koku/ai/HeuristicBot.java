package com.example.koku.ai;

import com.example.koku.ai.evaluator.BoardEvaluator;
import com.example.koku.ai.evaluator.ConnectFourEvaluator;
import com.example.koku.ai.evaluator.GomokuEvaluator;
import com.example.koku.ai.evaluator.TicTacToeEvaluator;
import com.example.koku.domain.Board;
import com.example.koku.domain.ForbiddenMoveChecker;
import com.example.koku.domain.GomokuEngine;
import com.example.koku.domain.Move;
import com.example.koku.domain.Player;
import com.example.koku.domain.Position;
import com.example.koku.domain.engine.GameEngine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class HeuristicBot implements Bot {
    private static final int MAX_GOMOKU_CANDIDATES = 120;

    private final RandomBot fallback;
    private final Random random;
    private final ForbiddenMoveChecker forbiddenMoveChecker;

    public HeuristicBot() {
        this(new RandomBot(), new Random());
    }

    public HeuristicBot(RandomBot fallback) {
        this(fallback, new Random());
    }

    public HeuristicBot(RandomBot fallback, Random random) {
        this.fallback = Objects.requireNonNull(fallback, "fallback cannot be null");
        this.random = Objects.requireNonNull(random, "random cannot be null");
        this.forbiddenMoveChecker = new ForbiddenMoveChecker();
    }

    @Override
    public BotMoveResult chooseMove(BotMoveRequest request) {
        Objects.requireNonNull(request, "request cannot be null");

        List<Move> legalMoves = candidateMoves(request, fallback.legalMoves(request));
        if (legalMoves.isEmpty()) {
            return BotMoveResult.noMove(BotDifficulty.NORMAL);
        }

        BoardEvaluator evaluator = evaluatorFor(request);
        int bestScore = Integer.MIN_VALUE;
        List<Move> bestMoves = new ArrayList<>();
        for (Move move : legalMoves) {
            int score = evaluator.evaluateMove(request, move);
            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (score == bestScore) {
                bestMoves.add(move);
            }
        }

        Move selectedMove = bestMoves.get(random.nextInt(bestMoves.size()));
        return BotMoveResult.of(selectedMove, BotDifficulty.NORMAL);
    }

    private BoardEvaluator evaluatorFor(BotMoveRequest request) {
        String gameId = request.gameDefinitionOptional()
                .map(definition -> definition.id())
                .orElse("");
        return switch (gameId) {
            case "ticTacToe" -> new TicTacToeEvaluator();
            case "connectFour" -> new ConnectFourEvaluator();
            // TODO: Add a dedicated Connect6 evaluator; this currently reuses Gomoku-style line scoring.
            case "sixInRow" -> new GomokuEvaluator(6);
            default -> new GomokuEvaluator();
        };
    }

    private List<Move> candidateMoves(BotMoveRequest request, List<Move> legalMoves) {
        if (legalMoves.isEmpty()) {
            return List.of();
        }

        String gameId = request.gameDefinitionOptional()
                .map(definition -> definition.id())
                .orElse("");
        if ("ticTacToe".equals(gameId) || "connectFour".equals(gameId)) {
            return legalMoves;
        }

        GameEngine engine = request.engine();
        if (!hasAnyStone(engine)) {
            return legalMoves.stream()
                    .max(Comparator.comparingInt(move -> centerScore(engine, move)))
                    .map(List::of)
                    .orElse(legalMoves);
        }

        BoardEvaluator evaluator = evaluatorFor(request);
        List<Move> nearbyMoves = legalMoves.stream()
                .filter(move -> isNearExistingStone(engine, move, 2))
                .filter(move -> isAllowedGomokuMove(request, move))
                .sorted(Comparator.comparingInt((Move move) -> evaluator.evaluateMove(request, move)).reversed())
                .limit(MAX_GOMOKU_CANDIDATES)
                .toList();
        if (!nearbyMoves.isEmpty()) {
            return nearbyMoves;
        }

        // TODO: Add safe engine simulation support for Gomoku shallow minimax, candidate move ordering,
        // and a background Task/thread when Gomoku search becomes expensive.
        List<Move> allowedMoves = legalMoves.stream()
                .filter(move -> isAllowedGomokuMove(request, move))
                .toList();
        return allowedMoves.isEmpty() ? legalMoves : allowedMoves;
    }

    private boolean isAllowedGomokuMove(BotMoveRequest request, Move move) {
        if (!(request.engine() instanceof GomokuEngine gomokuEngine)
                || !gomokuEngine.isForbiddenMovesEnabled()
                || request.botPlayer() != Player.BLACK) {
            return true;
        }

        Board boardCopy = copySquareBoard(request.engine());
        boardCopy.placeStone(Player.BLACK, move.position());
        return !forbiddenMoveChecker.isForbidden(boardCopy, move.position());
    }

    private Board copySquareBoard(GameEngine engine) {
        Board board = new Board(engine.getBoardSize());
        for (int row = 0; row < engine.getBoardRows(); row++) {
            for (int col = 0; col < engine.getBoardCols(); col++) {
                Player stone = engine.getStoneAt(new Position(row, col));
                if (stone != null) {
                    board.placeStone(stone, new Position(row, col));
                }
            }
        }
        return board;
    }

    private int quickGomokuScore(GameEngine engine, Move move) {
        return nearbyScore(engine, move, 2) * 10 + centerScore(engine, move);
    }

    private int nearbyScore(GameEngine engine, Move move, int radius) {
        int score = 0;
        int moveRow = move.position().row();
        int moveCol = move.position().col();
        for (int row = moveRow - radius; row <= moveRow + radius; row++) {
            for (int col = moveCol - radius; col <= moveCol + radius; col++) {
                if ((row == moveRow && col == moveCol)
                        || row < 0 || row >= engine.getBoardRows()
                        || col < 0 || col >= engine.getBoardCols()) {
                    continue;
                }
                if (engine.getStoneAt(new Position(row, col)) != null) {
                    int distance = Math.max(Math.abs(row - moveRow), Math.abs(col - moveCol));
                    score += Math.max(1, radius + 1 - distance);
                }
            }
        }
        return score;
    }

    private boolean hasAnyStone(GameEngine engine) {
        for (int row = 0; row < engine.getBoardRows(); row++) {
            for (int col = 0; col < engine.getBoardCols(); col++) {
                if (engine.getStoneAt(new Position(row, col)) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isNearExistingStone(GameEngine engine, Move move, int radius) {
        int moveRow = move.position().row();
        int moveCol = move.position().col();
        for (int row = moveRow - radius; row <= moveRow + radius; row++) {
            for (int col = moveCol - radius; col <= moveCol + radius; col++) {
                if ((row == moveRow && col == moveCol)
                        || row < 0 || row >= engine.getBoardRows()
                        || col < 0 || col >= engine.getBoardCols()) {
                    continue;
                }
                if (engine.getStoneAt(new Position(row, col)) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private int centerScore(GameEngine engine, Move move) {
        double centerRow = (engine.getBoardRows() - 1) / 2.0;
        double centerCol = (engine.getBoardCols() - 1) / 2.0;
        double distance = Math.abs(move.position().row() - centerRow) + Math.abs(move.position().col() - centerCol);
        return -(int) Math.round(distance * 100);
    }
}
