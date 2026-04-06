package com.example.koku.service;

public class ThemeService {
    public record Palette(
            boolean darkMode,
            String windowBg,
            String panelBg,
            String boardBg,
            String boardLine,
            String primaryText,
            String secondaryText,
            String subtleText,
            String buttonBg,
            String buttonBorder,
            String accent,
            String blackStone,
            String blackStoneBorder,
            String whiteStone,
            String whiteStoneBorder,
            String cardBorder
    ) {}

    public Palette getPalette(boolean darkMode) {
        if (darkMode) {
            return new Palette(
                    true,
                    "#181C22",
                    "#222831",
                    "#2B313A",
                    "#7F8998",
                    "#F3F6FA",
                    "#D8DFE8",
                    "#A7B0BE",
                    "#2D3440",
                    "#3B4350",
                    "#9CB2D0",
                    "#0E1116",
                    "#CDD6E3",
                    "#F5F7FA",
                    "#AEB7C4",
                    "#343B46"
            );
        }

        return new Palette(
                false,
                "#F4F2ED",
                "#FFFFFF",
                "#E6E0D6",
                "#8C857B",
                "#2B2B2B",
                "#57534E",
                "#8B867E",
                "#F8F8F6",
                "#D8D4CD",
                "#92A4BD",
                "#1D1D1F",
                null,
                "#FBFBF8",
                "#BDB7AE",
                "#E3DED6"
        );
    }
}