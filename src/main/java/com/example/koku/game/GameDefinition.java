package com.example.koku.game;

public record GameDefinition(
        String id,
        String titleKey,
        String descriptionKey,
        EngineFactory engineFactory,
        BoardViewFactory boardViewFactory
) {
}
