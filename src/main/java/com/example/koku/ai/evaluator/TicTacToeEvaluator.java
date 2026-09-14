package com.example.koku.ai.evaluator;

import com.example.koku.ai.BotMoveRequest;
import com.example.koku.domain.Move;
import com.example.koku.domain.Position;

public class TicTacToeEvaluator implements BoardEvaluator {
    @Override
    public int evaluateMove(BotMoveRequest request, Move move) {
        if (EvaluationSupport.completesLine(request.engine(), move, request.botPlayer(), 3)) {
            return EvaluationSupport.WIN_SCORE;
        }
        if (EvaluationSupport.completesLine(request.engine(), move, request.opponentPlayer(), 3)) {
            return EvaluationSupport.BLOCK_WIN_SCORE;
        }

        int score = 0;
        Position position = move.position();
        if (position.row() == 1 && position.col() == 1) {
            score += 1_000;
        } else if ((position.row() == 0 || position.row() == 2)
                && (position.col() == 0 || position.col() == 2)) {
            score += 450;
        } else {
            score += 100;
        }

        score += EvaluationSupport.linePotential(request.engine(), move, request.botPlayer(), 3);
        score += EvaluationSupport.linePotential(request.engine(), move, request.opponentPlayer(), 3) / 2;
        return score;
    }
}
