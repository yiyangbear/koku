package com.example.koku.game;

import com.example.koku.domain.GomokuEngine;
import com.example.koku.domain.SixInRowEngine;
import com.example.koku.domain.TicTacToeEngine;
import com.example.koku.ui.BoardCanvas;

import java.util.List;

public class GameRegistry {
    private GameRegistry() {}

    public static List<GameDefinition> all() {
        return List.of(
                ticTacToe(),
                gomoku(),
                sixInRow()
        );
    }

    public static GameDefinition ticTacToe() {
        return new GameDefinition(
                "ticTacToe",
                "game.ticTacToe.title",
                "game.ticTacToe.description",
                ruleConfig -> new TicTacToeEngine(),
                BoardCanvas::new
        );
    }

    public static GameDefinition gomoku() {
        return new GameDefinition(
                "gomoku",
                "game.gomoku.title",
                "game.gomoku.description",
                ruleConfig -> new GomokuEngine(
                        ruleConfig.boardSizeOption().size(),
                        ruleConfig.forbiddenMovesEnabled()
                ),
                BoardCanvas::new
        );
    }

    public static GameDefinition sixInRow() {
        return new GameDefinition(
                "sixInRow",
                "game.sixInRow.title",
                "game.sixInRow.description",
                ruleConfig -> new SixInRowEngine(19),
                BoardCanvas::new
        );
    }
}
