package com.example.koku.config;

public enum GameMode {
    HUMAN_VS_HUMAN,
    HUMAN_VS_COMPUTER;

    public String displayLabel(LanguageMode languageMode) {
        return switch (this) {
            case HUMAN_VS_HUMAN -> languageMode == LanguageMode.ZH_CN ? "人 vs 人" : "Human vs Human";
            case HUMAN_VS_COMPUTER -> languageMode == LanguageMode.ZH_CN ? "人 vs 机" : "Human vs Computer";
        };
    }
}
