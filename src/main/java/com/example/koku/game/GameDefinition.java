package com.example.koku.game;

public record GameDefinition(
        String id,
        String titleKey,
        String descriptionKey,
        boolean supportsBoardSize,
        boolean supportsForbiddenMoves,
        EngineFactory engineFactory,
        BoardViewFactory boardViewFactory
) {
}
