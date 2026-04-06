package com.example.koku.config;

public enum TimerOption {
    OFF(0),
    SEC_15(15),
    SEC_30(30),
    SEC_60(60),
    SEC_120(120);

    private final int seconds;

    TimerOption(int seconds) {
        this.seconds = seconds;
    }

    public int seconds() {
        return seconds;
    }

    public String displayLabel(LanguageMode languageMode) {
        if (this == OFF) {
            return languageMode == LanguageMode.ZH_CN ? "关闭" : "Off";
        }
        return seconds + "s";
    }
}