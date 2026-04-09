package com.example.koku.service;

import com.example.koku.config.TimerMode;
import com.example.koku.domain.Player;

public class TimerService {
    private TimerMode timerMode = TimerMode.PER_MOVE;
    private long perMoveLimitMillis = 0;
    private long totalLimitMillis = 0;
    private Player activePlayer;
    private long deadlineMillis = 0;
    private boolean running = false;
    private long remainingBlack = 0;
    private long remainingWhite = 0;
    private long lastTickMillis = 0;

    public void configure(TimerMode timerMode, long perMoveSeconds, long totalSeconds) {
        this.timerMode = timerMode == null ? TimerMode.PER_MOVE : timerMode;
        this.perMoveLimitMillis = Math.max(0, perMoveSeconds) * 1000L;
        this.totalLimitMillis = Math.max(0, totalSeconds) * 1000L;
        if (this.timerMode == TimerMode.TOTAL) {
            this.remainingBlack = totalLimitMillis;
            this.remainingWhite = totalLimitMillis;
        }
        stop();
    }

    public boolean isEnabled() {
        if (timerMode == TimerMode.TOTAL) {
            return totalLimitMillis > 0;
        }
        return perMoveLimitMillis > 0;
    }

    public void startTurn(Player player) {
        if (!isEnabled()) {
            stop();
            return;
        }
        long now = System.currentTimeMillis();
        if (timerMode == TimerMode.TOTAL) {
            if (running && activePlayer != null) {
                applyElapsed(activePlayer, now - lastTickMillis);
            }
            this.activePlayer = player;
            this.lastTickMillis = now;
            this.running = true;
            return;
        }

        this.activePlayer = player;
        this.deadlineMillis = now + perMoveLimitMillis;
        this.running = true;
    }

    public void stop() {
        this.activePlayer = null;
        this.deadlineMillis = 0;
        this.running = false;
        this.lastTickMillis = 0;
    }

    public long getRemainingMillis() {
        if (!isEnabled() || !running) {
            return 0;
        }
        if (timerMode == TimerMode.TOTAL) {
            return getRemainingMillis(activePlayer);
        }
        return Math.max(0, deadlineMillis - System.currentTimeMillis());
    }

    public long getRemainingMillis(Player player) {
        if (!isEnabled() || player == null) {
            return 0;
        }
        if (timerMode == TimerMode.PER_MOVE) {
            if (player == activePlayer && running) {
                return getRemainingMillis();
            }
            return perMoveLimitMillis;
        }

        long remaining = player == Player.BLACK ? remainingBlack : remainingWhite;
        if (running && activePlayer == player) {
            long now = System.currentTimeMillis();
            remaining -= (now - lastTickMillis);
        }
        return Math.max(0, remaining);
    }

    public Player getActivePlayer() {
        return activePlayer;
    }

    public Player checkTimeoutLoser() {
        if (!isEnabled() || !running || activePlayer == null) {
            return null;
        }
        if (timerMode == TimerMode.TOTAL) {
            long remaining = getRemainingMillis(activePlayer);
            if (remaining <= 0) {
                Player loser = activePlayer;
                stop();
                return loser;
            }
            return null;
        }
        if (System.currentTimeMillis() >= deadlineMillis) {
            Player loser = activePlayer;
            stop();
            return loser;
        }
        return null;
    }

    private void applyElapsed(Player player, long elapsed) {
        if (elapsed <= 0) {
            return;
        }
        if (player == Player.BLACK) {
            remainingBlack = Math.max(0, remainingBlack - elapsed);
        } else {
            remainingWhite = Math.max(0, remainingWhite - elapsed);
        }
    }
}
