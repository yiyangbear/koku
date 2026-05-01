package com.example.koku.app;

import com.example.koku.config.LanguageMode;
import com.example.koku.game.GameDefinition;

public record MenuActions(
        Runnable newMatch,
        Runnable undo,
        Runnable backToSelect,
        Runnable openSettings,
        java.util.function.Consumer<Boolean> showCoordinates,
        java.util.function.Consumer<Boolean> showLastMoveMarker,
        Runnable toggleTheme,
        java.util.function.Consumer<LanguageMode> setLanguage,
        java.util.function.Consumer<GameDefinition> switchGame,
        Runnable showRules,
        Runnable showShortcuts,
        Runnable showAbout,
        Runnable quit
) {
}
