package com.example.koku.config;

public enum TotalTimerOption {
    OFF(0),
    MIN_5(5),
    MIN_10(10),
    MIN_15(15),
    MIN_30(30),
    MIN_45(45),
    MIN_60(60),
    CUSTOM(-1);

    private final int minutes;

    TotalTimerOption(int minutes) {
        this.minutes = minutes;
    }

    public int minutes() {
        return minutes;
    }

    public int seconds() {
        return minutes * 60;
    }

    public String displayLabel(LanguageMode languageMode) {
        if (this == OFF) {
            return languageMode == LanguageMode.ZH_CN ? "关闭" : "Off";
        }
        if (this == CUSTOM) {
            return languageMode == LanguageMode.ZH_CN ? "自定义..." : "Custom...";
        }
        return languageMode == LanguageMode.ZH_CN ? minutes + "分钟" : minutes + " min";
    }
}
