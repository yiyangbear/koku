package com.example.koku.config;

public enum PlayerOrder {
    PLAYER_FIRST,
    PLAYER_SECOND;

    public String displayLabel(LanguageMode languageMode) {
        return switch (this) {
            case PLAYER_FIRST -> languageMode == LanguageMode.ZH_CN ? "玩家先手" : "Player First";
            case PLAYER_SECOND -> languageMode == LanguageMode.ZH_CN ? "玩家后手" : "Player Second";
        };
    }
}
