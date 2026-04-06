package com.example.koku.service;

import com.example.koku.config.TimerOption;
import com.example.koku.domain.Player;

public class TimerService {
    private TimerOption timerOption = TimerOption.OFF;
    private long turnLimitMillis = 0;
    private Player activePlayer;
    private long deadlineMillis = 0;
    private boolean running = false;

    public void configure(TimerOption timerOption) {
        this.timerOption = timerOption;
        this.turnLimitMillis = timerOption.seconds() * 1000L;
        stop();
    }

    public boolean isEnabled() {
        return timerOption != null && timerOption.seconds() > 0;
    }

    public void startTurn(Player player) {
        if (!isEnabled()) {
            stop();
            return;
        }
        this.activePlayer = player;
        this.deadlineMillis = System.currentTimeMillis() + turnLimitMillis;
        this.running = true;
    }

    public void stop() {
        this.activePlayer = null;
        this.deadlineMillis = 0;
        this.running = false;
    }

    public long getRemainingMillis() {
        if (!isEnabled() || !running) {
            return 0;
        }
        return Math.max(0, deadlineMillis - System.currentTimeMillis());
    }

    public Player getActivePlayer() {
        return activePlayer;
    }

    public Player checkTimeoutLoser() {
        if (!isEnabled() || !running || activePlayer == null) {
            return null;
        }
        if (System.currentTimeMillis() >= deadlineMillis) {
            Player loser = activePlayer;
            stop();
            return loser;
        }
        return null;
    }
}