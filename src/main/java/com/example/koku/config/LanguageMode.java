package com.example.koku.config;

public enum LanguageMode {
    ZH_CN,
    EN_US;

    public String displayLabel(LanguageMode languageMode) {
        return switch (this) {
            case ZH_CN -> languageMode == LanguageMode.ZH_CN ? "简体中文" : "Simplified Chinese";
            case EN_US -> "English";
        };
    }
}