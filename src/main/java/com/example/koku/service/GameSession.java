package com.example.koku.service;

import com.example.koku.config.RuleConfig;
import com.example.koku.config.TimerMode;
import com.example.koku.config.TimerOption;
import com.example.koku.config.TotalTimerOption;
import com.example.koku.domain.GameResult;
import com.example.koku.domain.GameStatus;
import com.example.koku.domain.engine.GameEngine;
import com.example.koku.game.EngineFactory;
import com.example.koku.domain.Move;
import com.example.koku.domain.Player;
import com.example.koku.domain.Position;

import java.util.Optional;

public class GameSession {
    private GameEngine engine;
    private RuleConfig ruleConfig;
    private final TimerService timerService;
    private final EngineFactory engineFactory;

    public GameSession(RuleConfig ruleConfig, EngineFactory engineFactory) {
        this.ruleConfig = ruleConfig;
        this.engineFactory = engineFactory;
        this.engine = engineFactory.create(ruleConfig);
        this.timerService = new TimerService();
        this.timerService.configure(
                ruleConfig.timerMode(),
                resolvePerMoveSeconds(ruleConfig),
                resolveTotalSeconds(ruleConfig)
        );
        this.timerService.startTurn(Player.BLACK);
    }

    public void applyRuleConfigAndNewMatch(RuleConfig ruleConfig) {
        this.ruleConfig = ruleConfig;
        this.engine = engineFactory.create(ruleConfig);
        this.timerService.configure(
                ruleConfig.timerMode(),
                resolvePerMoveSeconds(ruleConfig),
                resolveTotalSeconds(ruleConfig)
        );
        this.timerService.startTurn(Player.BLACK);
    }

    public RuleConfig getRuleConfig() {
        return ruleConfig;
    }

    public int getBoardSize() {
        return engine.getBoardSize();
    }

    public int getBoardRows() {
        return engine.getBoardRows();
    }

    public int getBoardCols() {
        return engine.getBoardCols();
    }

    public Player getCurrentPlayer() {
        return engine.getCurrentPlayer();
    }

    public boolean makeMove(int row, int col) {
        if (checkTimeout()) {
            return false;
        }

        boolean success = engine.makeMove(new Position(row, col));
        if (!success) {
            return false;
        }

        if (engine.isGameOver()) {
            timerService.stop();
        } else {
            timerService.startTurn(engine.getCurrentPlayer());
        }
        return true;
    }

    public boolean undo() {
        boolean success = engine.undo();
        if (!success) {
            return false;
        }

        if (engine.isGameOver()) {
            timerService.stop();
        } else {
            timerService.startTurn(engine.getCurrentPlayer());
        }
        return true;
    }

    public void newMatch() {
        this.engine = engineFactory.create(ruleConfig);
        this.timerService.configure(
                ruleConfig.timerMode(),
                resolvePerMoveSeconds(ruleConfig),
                resolveTotalSeconds(ruleConfig)
        );
        this.timerService.startTurn(Player.BLACK);
    }

    public boolean checkTimeout() {
        if (engine.isGameOver()) {
            return false;
        }

        Player loser = timerService.checkTimeoutLoser();
        if (loser != null) {
            engine.forceTimeoutLoss(loser);
            return true;
        }
        return false;
    }

    public boolean isTimerEnabled() {
        return timerService.isEnabled();
    }

    public long getTurnRemainingMillis() {
        return timerService.getRemainingMillis();
    }

    public long getPlayerRemainingMillis(Player player) {
        return timerService.getRemainingMillis(player);
    }

    private long resolvePerMoveSeconds(RuleConfig ruleConfig) {
        if (ruleConfig.timerMode() != TimerMode.PER_MOVE) {
            return 0;
        }
        if (ruleConfig.perMoveTimerOption() == TimerOption.CUSTOM) {
            return (long) ruleConfig.perMoveCustomMinutes() * 60L + ruleConfig.perMoveCustomSeconds();
        }
        return ruleConfig.perMoveTimerOption().seconds();
    }

    private long resolveTotalSeconds(RuleConfig ruleConfig) {
        if (ruleConfig.timerMode() != TimerMode.TOTAL) {
            return 0;
        }
        if (ruleConfig.totalTimerOption() == TotalTimerOption.CUSTOM) {
            return (long) ruleConfig.totalCustomMinutes() * 60L + ruleConfig.totalCustomSeconds();
        }
        return ruleConfig.totalTimerOption().seconds();
    }

    public Player getTimerActivePlayer() {
        return timerService.getActivePlayer();
    }

    public String getLatestMessage() {
        return engine.getLatestMessage();
    }

    public Optional<Move> getLastMove() {
        return engine.getLastMove();
    }

    public Player getStoneAt(int row, int col) {
        return engine.getStoneAt(new Position(row, col));
    }

    public boolean isGameOver() {
        return engine.isGameOver();
    }

    public GameResult getResult() {
        return engine.getResult();
    }

    public String getLastMoveCoordinate() {
        return getLastMove()
                .map(move -> formatPosition(move.position()))
                .orElse("-");
    }

    public String getTopStatusKey() {
        GameStatus status = engine.getResult().status();
        if (status == GameStatus.BLACK_WIN) return "result.blackWins";
        if (status == GameStatus.WHITE_WIN) return "result.whiteWins";
        if (status == GameStatus.DRAW) return "result.draw";
        return engine.getCurrentPlayer() == Player.BLACK
                ? "status.blackToMove"
                : "status.whiteToMove";
    }

    public String getResultDetailKey() {
        String reason = engine.getResult().reason();
        if ("timeout.black".equals(reason)) {
            return "result.blackTimeoutWhiteWins";
        }
        if ("timeout.white".equals(reason)) {
            return "result.whiteTimeoutBlackWins";
        }
        return null;
    }

    public String formatPosition(Position position) {
        char colChar = (char) ('A' + position.col());
        int rowNumber = position.row() + 1;
        return colChar + String.valueOf(rowNumber);
    }

    public String boardSizeLabel() {
        return engine.getBoardCols() + "x" + engine.getBoardRows();
    }
}
