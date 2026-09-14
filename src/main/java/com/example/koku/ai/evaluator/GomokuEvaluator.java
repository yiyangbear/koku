package com.example.koku.ai.evaluator;

import com.example.koku.ai.BotMoveRequest;
import com.example.koku.domain.Move;
import com.example.koku.domain.Player;
import com.example.koku.domain.Position;
import com.example.koku.domain.engine.GameEngine;

public class GomokuEvaluator implements BoardEvaluator {
    private static final int OFFENSE_OPEN_FOUR = 200_000;
    private static final int DEFENSE_OPEN_FOUR = 180_000;
    private static final int OFFENSE_DOUBLE_FOUR = 160_000;
    private static final int DEFENSE_DOUBLE_FOUR = 150_000;
    private static final int OFFENSE_CLOSED_FOUR = 50_000;
    private static final int DEFENSE_CLOSED_FOUR = 45_000;
    private static final int OFFENSE_OPEN_THREE = 12_000;
    private static final int DEFENSE_OPEN_THREE = 11_000;
    private static final int OFFENSE_DOUBLE_THREE = 30_000;
    private static final int DEFENSE_DOUBLE_THREE = 28_000;
    private static final int BLOCKED_THREE = 3_000;
    private static final int OPEN_TWO = 800;

    private final int targetLength;

    public GomokuEvaluator() {
        this(5);
    }

    public GomokuEvaluator(int targetLength) {
        this.targetLength = targetLength;
    }

    @Override
    public int evaluateMove(BotMoveRequest request, Move move) {
        if (EvaluationSupport.completesLine(request.engine(), move, request.botPlayer(), targetLength)) {
            return EvaluationSupport.WIN_SCORE;
        }
        if (EvaluationSupport.completesLine(request.engine(), move, request.opponentPlayer(), targetLength)) {
            return EvaluationSupport.BLOCK_WIN_SCORE;
        }

        PatternSummary bot = summarize(request.engine(), move, request.botPlayer());
        PatternSummary opponent = summarize(request.engine(), move, request.opponentPlayer());

        int score = scoreSummary(bot, true) + scoreSummary(opponent, false);
        score += EvaluationSupport.nearbyStoneScore(request.engine(), move, 2) * 10;
        score += Math.min(99, EvaluationSupport.centerScore(request.engine(), move));
        // TODO: Add better forbidden-move-aware evaluation and VCF/VCT threat search before Gomoku shallow minimax.
        return score;
    }

    private int scoreSummary(PatternSummary summary, boolean offense) {
        int score = 0;
        if (summary.openFours() >= 2 || summary.openFours() + summary.closedFours() >= 2) {
            score += offense ? OFFENSE_DOUBLE_FOUR : DEFENSE_DOUBLE_FOUR;
        }
        if (summary.openThrees() >= 2) {
            score += offense ? OFFENSE_DOUBLE_THREE : DEFENSE_DOUBLE_THREE;
        }

        score += summary.openFours() * (offense ? OFFENSE_OPEN_FOUR : DEFENSE_OPEN_FOUR);
        score += summary.closedFours() * (offense ? OFFENSE_CLOSED_FOUR : DEFENSE_CLOSED_FOUR);
        score += summary.openThrees() * (offense ? OFFENSE_OPEN_THREE : DEFENSE_OPEN_THREE);
        score += summary.blockedThrees() * BLOCKED_THREE;
        score += summary.openTwos() * OPEN_TWO;
        return score;
    }

    private PatternSummary summarize(GameEngine engine, Move move, Player player) {
        int openFours = 0;
        int closedFours = 0;
        int openThrees = 0;
        int blockedThrees = 0;
        int openTwos = 0;

        for (int[] direction : EvaluationSupport.directions()) {
            String line = directionalLine(engine, move, player, direction[0], direction[1]);

            if (containsAny(line, ".XXXX.", ".XXX.X.", ".XX.XX.", ".X.XXX.")) {
                openFours++;
            } else if (containsAny(line, "OXXXX.", ".XXXXO", "OXXX.X.", ".X.XXXO", "OXX.XX.", ".XX.XXO",
                    "OXX.XXO", "OXXX.XO", "OX.XXXO")) {
                closedFours++;
            }

            if (containsAny(line, ".XXX.", ".XX.X.", ".X.XX.")) {
                openThrees++;
            } else if (containsAny(line, "OXXX.", ".XXXO", "OXX.X.", ".X.XXO", "OX.XX.", ".XX.XO")) {
                blockedThrees++;
            }

            if (containsAny(line, ".XX.", ".X.X.")) {
                openTwos++;
            }
        }

        return new PatternSummary(openFours, closedFours, openThrees, blockedThrees, openTwos);
    }

    private String directionalLine(GameEngine engine, Move move, Player player, int dr, int dc) {
        StringBuilder line = new StringBuilder();
        for (int offset = -5; offset <= 5; offset++) {
            int row = move.position().row() + dr * offset;
            int col = move.position().col() + dc * offset;
            if (!EvaluationSupport.isInside(engine, row, col)) {
                line.append('O');
                continue;
            }

            Position position = move.position();
            Player stone = position.row() == row && position.col() == col
                    ? player
                    : engine.getStoneAt(new Position(row, col));
            if (stone == player) {
                line.append('X');
            } else if (stone == null) {
                line.append('.');
            } else {
                line.append('O');
            }
        }
        return line.toString();
    }

    private boolean containsAny(String line, String... patterns) {
        for (String pattern : patterns) {
            if (line.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private record PatternSummary(
            int openFours,
            int closedFours,
            int openThrees,
            int blockedThrees,
            int openTwos
    ) {
    }
}
