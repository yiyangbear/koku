package com.example.koku.ui.boards;

import com.example.koku.service.ThemeService;
import javafx.scene.Node;

public interface GameBoardView {
    Node getNode();

    void configure(ThemeService.Palette palette, boolean showCoordinates, boolean showLastMoveMarker, String fontFamily);

    void draw();

    void setOnBoardChanged(Runnable onBoardChanged);
}
