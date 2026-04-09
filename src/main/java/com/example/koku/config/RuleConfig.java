package com.example.koku.config;

public record RuleConfig(
        BoardSizeOption boardSizeOption,
        boolean forbiddenMovesEnabled,
        TimerOption perMoveTimerOption,
        int perMoveCustomMinutes,
        int perMoveCustomSeconds,
        TotalTimerOption totalTimerOption,
        int totalCustomMinutes,
        int totalCustomSeconds,
        TimerMode timerMode
) {
    public static RuleConfig defaultConfig() {
        return new RuleConfig(
                BoardSizeOption.SIZE_15,
                false,
                TimerOption.OFF,
                0,
                0,
                TotalTimerOption.OFF,
                0,
                0,
                TimerMode.PER_MOVE
        );
    }
}
