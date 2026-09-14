package com.example.koku.ai.evaluator;

import com.example.koku.ai.BotMoveRequest;
import com.example.koku.domain.Move;
import com.example.koku.domain.Player;
import com.example.koku.domain.engine.GameEngine;

public class ConnectFourEvaluator implements BoardEvaluator {
    private static final int WINDOW_SIZE = 4;

    @Override
    public int evaluateMove(BotMoveRequest request, Move move) {
        if (EvaluationSupport.completesLine(request.engine(), move, request.botPlayer(), WINDOW_SIZE)) {
            return EvaluationSupport.WIN_SCORE;
        }
        if (EvaluationSupport.completesLine(request.engine(), move, request.opponentPlayer(), WINDOW_SIZE)) {
            return EvaluationSupport.BLOCK_WIN_SCORE;
        }

        int score = centerColumnScore(request.engine(), move);
        score += scoreWindows(request.engine(), move, request.botPlayer(), request.opponentPlayer());
        score += scoreWindows(request.engine(), move, request.opponentPlayer(), request.botPlayer()) / 2;
        // TODO: Improve Connect Four evaluation with gravity-aware multi-ply threat detection.
        return score;
    }

    private int centerColumnScore(GameEngine engine, Move move) {
        double center = (engine.getBoardCols() - 1) / 2.0;
        return Math.max(0, 80 - (int) Math.round(Math.abs(move.position().col() - center) * 20));
    }

    private int scoreWindows(GameEngine engine, Move move, Player player, Player opponent) {
        int score = 0;
        for (int[] direction : EvaluationSupport.directions()) {
            for (int offset = -(WINDOW_SIZE - 1); offset <= 0; offset++) {
                int playerCount = 0;
                int opponentCount = 0;
                int emptyCount = 0;

                for (int step = 0; step < WINDOW_SIZE; step++) {
                    int row = move.position().row() + (offset + step) * direction[0];
                    int col = move.position().col() + (offset + step) * direction[1];
                    if (!EvaluationSupport.isInside(engine, row, col)) {
                        playerCount = -1;
                        break;
                    }

                    Player stone = EvaluationSupport.stoneAt(engine, move, player, row, col);
                    if (stone == player) {
                        playerCount++;
                    } else if (stone == opponent) {
                        opponentCount++;
                    } else {
                        emptyCount++;
                    }
                }

                if (playerCount < 0 || opponentCount > 0) {
                    continue;
                }
                score += windowScore(playerCount, emptyCount);
            }
        }
        return score;
    }

    private int windowScore(int playerCount, int emptyCount) {
        if (playerCount == 4) {
            return 100_000;
        }
        if (playerCount == 3 && emptyCount == 1) {
            return 8_000;
        }
        if (playerCount == 2 && emptyCount == 2) {
            return 800;
        }
        if (playerCount == 1 && emptyCount == 3) {
            return 80;
        }
        return 0;
    }
}
