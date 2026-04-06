package com.example.koku.config;

public enum ThemeMode {
    LIGHT,
    DARK;

    public String displayLabel(LanguageMode languageMode) {
        return switch (this) {
            case LIGHT -> languageMode == LanguageMode.ZH_CN ? "浅色" : "Light";
            case DARK -> languageMode == LanguageMode.ZH_CN ? "深色" : "Dark";
        };
    }
}