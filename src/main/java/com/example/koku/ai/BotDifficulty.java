package com.example.koku.ai;

import com.example.koku.config.LanguageMode;

public enum BotDifficulty {
    EASY,
    NORMAL,
    HARD;

    public String displayLabel(LanguageMode languageMode) {
        return switch (this) {
            case EASY -> languageMode == LanguageMode.ZH_CN ? "简单" : "Easy";
            case NORMAL -> languageMode == LanguageMode.ZH_CN ? "普通" : "Normal";
            case HARD -> languageMode == LanguageMode.ZH_CN ? "困难" : "Hard";
        };
    }
}
