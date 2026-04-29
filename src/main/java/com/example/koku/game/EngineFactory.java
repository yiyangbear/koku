package com.example.koku.game;

import com.example.koku.config.RuleConfig;
import com.example.koku.domain.engine.GameEngine;

@FunctionalInterface
public interface EngineFactory {
    GameEngine create(RuleConfig ruleConfig);
}
