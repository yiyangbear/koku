package com.example.koku.config;

public record RuleConfig(
        BoardSizeOption boardSizeOption,
        boolean forbiddenMovesEnabled,
        TimerOption timerOption
) {
    public static RuleConfig defaultConfig() {
        return new RuleConfig(
                BoardSizeOption.SIZE_15,
                false,
                TimerOption.OFF
        );
    }
}