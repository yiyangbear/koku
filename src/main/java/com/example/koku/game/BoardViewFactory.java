package com.example.koku.game;

import com.example.koku.service.GameSession;
import com.example.koku.ui.boards.GameBoardView;

@FunctionalInterface
public interface BoardViewFactory {
    GameBoardView create(GameSession session);
}
