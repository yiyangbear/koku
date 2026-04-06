package com.example.koku.config;

public enum BoardSizeOption {
    SIZE_15(15),
    SIZE_19(19);

    private final int size;

    BoardSizeOption(int size) {
        this.size = size;
    }

    public int size() {
        return size;
    }

    public String displayLabel(LanguageMode languageMode) {
        if (this == SIZE_15) {
            return languageMode == LanguageMode.ZH_CN
                    ? "15x15（默认）"
                    : "15x15 (Default)";
        }
        return "19x19";
    }
}