package com.example.koku.config;

public enum TimerMode {
    PER_MOVE,
    TOTAL;

    public String displayLabel(LanguageMode languageMode) {
        return switch (this) {
            case PER_MOVE -> languageMode == LanguageMode.ZH_CN ? "每手计时" : "Per Move";
            case TOTAL -> languageMode == LanguageMode.ZH_CN ? "全局计时" : "Total";
        };
    }
}
