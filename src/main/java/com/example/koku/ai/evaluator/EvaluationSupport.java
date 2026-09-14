package com.example.koku.ai.evaluator;

import com.example.koku.domain.Move;
import com.example.koku.domain.Player;
import com.example.koku.domain.Position;
import com.example.koku.domain.engine.GameEngine;

final class EvaluationSupport {
    static final int WIN_SCORE = 1_000_000;
    static final int BLOCK_WIN_SCORE = 900_000;

    private static final int[][] DIRECTIONS = {
            {1, 0},
            {0, 1},
            {1, 1},
            {1, -1}
    };

    private EvaluationSupport() {}

    static int[][] directions() {
        return DIRECTIONS;
    }

    static boolean isInside(GameEngine engine, int row, int col) {
        return row >= 0 && row < engine.getBoardRows()
                && col >= 0 && col < engine.getBoardCols();
    }

    static Player stoneAt(GameEngine engine, Move virtualMove, Player virtualPlayer, int row, int col) {
        if (!isInside(engine, row, col)) {
            return null;
        }
        Position position = virtualMove.position();
        if (position.row() == row && position.col() == col) {
            return virtualPlayer;
        }
        return engine.getStoneAt(new Position(row, col));
    }

    static boolean completesLine(GameEngine engine, Move move, Player player, int targetLength) {
        for (int[] direction : DIRECTIONS) {
            int count = 1
                    + countDirection(engine, move, player, direction[0], direction[1])
                    + countDirection(engine, move, player, -direction[0], -direction[1]);
            if (count >= targetLength) {
                return true;
            }
        }
        return false;
    }

    static int linePotential(GameEngine engine, Move move, Player player, int targetLength) {
        int score = 0;
        for (int[] direction : DIRECTIONS) {
            int forward = countDirection(engine, move, player, direction[0], direction[1]);
            int backward = countDirection(engine, move, player, -direction[0], -direction[1]);
            int stones = 1 + forward + backward;
            int openEnds = openEnd(engine, move, player, direction[0], direction[1], forward)
                    + openEnd(engine, move, player, -direction[0], -direction[1], backward);
            score += patternScore(stones, openEnds, targetLength);
        }
        return score;
    }

    static int nearbyStoneScore(GameEngine engine, Move move, int radius) {
        int score = 0;
        int moveRow = move.position().row();
        int moveCol = move.position().col();
        for (int row = moveRow - radius; row <= moveRow + radius; row++) {
            for (int col = moveCol - radius; col <= moveCol + radius; col++) {
                if ((row == moveRow && col == moveCol) || !isInside(engine, row, col)) {
                    continue;
                }
                if (engine.getStoneAt(new Position(row, col)) != null) {
                    int distance = Math.max(Math.abs(row - moveRow), Math.abs(col - moveCol));
                    score += Math.max(1, radius + 1 - distance) * 8;
                }
            }
        }
        return score;
    }

    static int centerScore(GameEngine engine, Move move) {
        double centerRow = (engine.getBoardRows() - 1) / 2.0;
        double centerCol = (engine.getBoardCols() - 1) / 2.0;
        double distance = Math.abs(move.position().row() - centerRow) + Math.abs(move.position().col() - centerCol);
        return Math.max(0, 24 - (int) Math.round(distance * 3));
    }

    static boolean hasAnyStone(GameEngine engine) {
        for (int row = 0; row < engine.getBoardRows(); row++) {
            for (int col = 0; col < engine.getBoardCols(); col++) {
                if (engine.getStoneAt(new Position(row, col)) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean isNearExistingStone(GameEngine engine, Move move, int radius) {
        int moveRow = move.position().row();
        int moveCol = move.position().col();
        for (int row = moveRow - radius; row <= moveRow + radius; row++) {
            for (int col = moveCol - radius; col <= moveCol + radius; col++) {
                if ((row == moveRow && col == moveCol) || !isInside(engine, row, col)) {
                    continue;
                }
                if (engine.getStoneAt(new Position(row, col)) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int countDirection(GameEngine engine, Move move, Player player, int dr, int dc) {
        int count = 0;
        int row = move.position().row() + dr;
        int col = move.position().col() + dc;
        while (isInside(engine, row, col) && stoneAt(engine, move, player, row, col) == player) {
            count++;
            row += dr;
            col += dc;
        }
        return count;
    }

    private static int openEnd(GameEngine engine, Move move, Player player, int dr, int dc, int stonesInDirection) {
        int row = move.position().row() + dr * (stonesInDirection + 1);
        int col = move.position().col() + dc * (stonesInDirection + 1);
        return isInside(engine, row, col) && stoneAt(engine, move, player, row, col) == null ? 1 : 0;
    }

    private static int patternScore(int stones, int openEnds, int targetLength) {
        if (stones >= targetLength) {
            return WIN_SCORE;
        }
        if (openEnds == 0) {
            return 0;
        }
        int missing = targetLength - stones;
        if (missing == 1) {
            return openEnds == 2 ? 90_000 : 45_000;
        }
        if (missing == 2) {
            return openEnds == 2 ? 10_000 : 4_000;
        }
        if (missing == 3) {
            return openEnds == 2 ? 1_200 : 400;
        }
        return openEnds == 2 ? 120 : 40;
    }
}
