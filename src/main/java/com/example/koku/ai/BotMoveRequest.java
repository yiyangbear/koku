package com.example.koku.ai;

import com.example.koku.domain.Player;
import com.example.koku.domain.engine.GameEngine;
import com.example.koku.game.GameDefinition;

import java.util.Objects;
import java.util.Optional;

public record BotMoveRequest(
        GameEngine engine,
        Player botPlayer,
        Player opponentPlayer,
        GameDefinition gameDefinition
) {
    public BotMoveRequest {
        Objects.requireNonNull(engine, "engine cannot be null");
        Objects.requireNonNull(botPlayer, "botPlayer cannot be null");
        Objects.requireNonNull(opponentPlayer, "opponentPlayer cannot be null");
    }

    public BotMoveRequest(GameEngine engine, Player botPlayer, GameDefinition gameDefinition) {
        this(engine, botPlayer, botPlayer.opposite(), gameDefinition);
    }

    public BotMoveRequest(GameEngine engine, Player botPlayer) {
        this(engine, botPlayer, botPlayer.opposite(), null);
    }

    public Optional<GameDefinition> gameDefinitionOptional() {
        return Optional.ofNullable(gameDefinition);
    }
}
