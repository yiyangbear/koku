package com.example.koku.ai.evaluator;

import com.example.koku.ai.BotMoveRequest;
import com.example.koku.domain.Move;

public interface BoardEvaluator {
    int evaluateMove(BotMoveRequest request, Move move);
}
