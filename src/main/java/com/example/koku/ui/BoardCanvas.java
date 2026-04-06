package com.example.koku.ui;

import com.example.koku.domain.Move;
import com.example.koku.domain.Player;
import com.example.koku.service.GameSession;
import com.example.koku.service.ThemeService;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Optional;

public class BoardCanvas extends Canvas {
    private final GameSession session;

    private ThemeService.Palette palette;
    private boolean showCoordinates;
    private boolean showLastMoveMarker;

    private Runnable onBoardChanged;

    private static final double CANVAS_SIZE = 780;
    private static final double PADDING = 56;

    public BoardCanvas(GameSession session) {
        super(CANVAS_SIZE, CANVAS_SIZE);
        this.session = session;
        setOnMouseClicked(this::handleMouseClicked);
    }

    public void configure(ThemeService.Palette palette, boolean showCoordinates, boolean showLastMoveMarker) {
        this.palette = palette;
        this.showCoordinates = showCoordinates;
        this.showLastMoveMarker = showLastMoveMarker;
        draw();
    }

    public void setOnBoardChanged(Runnable onBoardChanged) {
        this.onBoardChanged = onBoardChanged;
    }

    public void draw() {
        if (palette == null) {
            return;
        }

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        gc.setFill(Color.web(palette.boardBg()));
        gc.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);

        drawGrid(gc);
        drawCenterStar(gc);

        if (showCoordinates) {
            drawCoordinates(gc);
        }

        drawStones(gc);

        if (showLastMoveMarker) {
            drawLastMoveMarker(gc);
        }
    }

    private void drawGrid(GraphicsContext gc) {
        int size = session.getBoardSize();
        double cell = getCellSize();

        gc.setStroke(Color.web(palette.boardLine()));
        gc.setLineWidth(1.0);

        for (int i = 0; i < size; i++) {
            double pos = PADDING + i * cell;

            gc.strokeLine(PADDING, pos, PADDING + cell * (size - 1), pos);
            gc.strokeLine(pos, PADDING, pos, PADDING + cell * (size - 1));
        }
    }

    private void drawCenterStar(GraphicsContext gc) {
        int size = session.getBoardSize();
        if (size % 2 == 0) {
            return;
        }

        double cell = getCellSize();
        int center = size / 2;
        double x = PADDING + center * cell;
        double y = PADDING + center * cell;

        gc.setFill(Color.web(palette.boardLine()));
        gc.fillOval(x - 4, y - 4, 8, 8);
    }

    private void drawCoordinates(GraphicsContext gc) {
        int size = session.getBoardSize();
        double cell = getCellSize();

        gc.setFill(Color.web(palette.subtleText()));
        gc.setFont(Font.font(11));

        for (int i = 0; i < size; i++) {
            String colText = String.valueOf((char) ('A' + i));
            String rowText = String.valueOf(i + 1);

            double pos = PADDING + i * cell;
            gc.fillText(colText, pos - 4, PADDING - 18);
            gc.fillText(colText, pos - 4, PADDING + cell * (size - 1) + 24);

            gc.fillText(rowText, PADDING - 26, pos + 4);
            gc.fillText(rowText, PADDING + cell * (size - 1) + 12, pos + 4);
        }
    }

    private void drawStones(GraphicsContext gc) {
        int size = session.getBoardSize();
        double cell = getCellSize();
        double stoneSize = cell * 0.78;

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Player stone = session.getStoneAt(row, col);
                if (stone == null) {
                    continue;
                }

                double centerX = PADDING + col * cell;
                double centerY = PADDING + row * cell;
                double x = centerX - stoneSize / 2.0;
                double y = centerY - stoneSize / 2.0;

                if (stone == Player.BLACK) {
                    gc.setFill(Color.web(palette.blackStone()));
                    gc.fillOval(x, y, stoneSize, stoneSize);

                    if (palette.darkMode() && palette.blackStoneBorder() != null) {
                        gc.setStroke(Color.web(palette.blackStoneBorder()));
                        gc.setLineWidth(1.2);
                        gc.strokeOval(x, y, stoneSize, stoneSize);
                    }
                } else {
                    gc.setFill(Color.web(palette.whiteStone()));
                    gc.fillOval(x, y, stoneSize, stoneSize);

                    gc.setStroke(Color.web(palette.whiteStoneBorder()));
                    gc.setLineWidth(1.0);
                    gc.strokeOval(x, y, stoneSize, stoneSize);
                }
            }
        }
    }

    private void drawLastMoveMarker(GraphicsContext gc) {
        Optional<Move> lastMove = session.getLastMove();
        if (lastMove.isEmpty()) {
            return;
        }

        double cell = getCellSize();
        double centerX = PADDING + lastMove.get().position().col() * cell;
        double centerY = PADDING + lastMove.get().position().row() * cell;

        gc.setFill(Color.web(palette.accent()));
        gc.fillOval(centerX - 4, centerY - 4, 8, 8);
    }

    private void handleMouseClicked(MouseEvent event) {
        if (session.isGameOver()) {
            notifyBoardChanged();
            return;
        }

        int size = session.getBoardSize();
        double cell = getCellSize();

        int col = (int) Math.round((event.getX() - PADDING) / cell);
        int row = (int) Math.round((event.getY() - PADDING) / cell);

        if (row < 0 || row >= size || col < 0 || col >= size) {
            notifyBoardChanged();
            return;
        }

        session.makeMove(row, col);
        draw();
        notifyBoardChanged();
    }

    private double getCellSize() {
        int size = session.getBoardSize();
        return (CANVAS_SIZE - PADDING * 2) / (size - 1);
    }

    private void notifyBoardChanged() {
        if (onBoardChanged != null) {
            onBoardChanged.run();
        }
    }
}