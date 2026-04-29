package com.example.koku.ui;

import com.example.koku.domain.Move;
import com.example.koku.domain.Player;
import com.example.koku.service.GameSession;
import com.example.koku.service.ThemeService;
import com.example.koku.ui.boards.GameBoardView;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Optional;

public class BoardCanvas extends Canvas implements GameBoardView {
    private final GameSession session;

    private ThemeService.Palette palette;
    private boolean showCoordinates;
    private boolean showLastMoveMarker;
    private String fontFamily;

    private Runnable onBoardChanged;

    private static final double CANVAS_SIZE = 780;
    private static final double PADDING = 56;
    private static final double SMALL_BOARD_AREA = 360;
    private static final double SMALL_BOARD_PADDING = 86;

    public BoardCanvas(GameSession session) {
        super(CANVAS_SIZE, CANVAS_SIZE);
        this.session = session;
        setOnMouseClicked(this::handleMouseClicked);
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public void configure(ThemeService.Palette palette, boolean showCoordinates, boolean showLastMoveMarker, String fontFamily) {
        this.palette = palette;
        this.showCoordinates = showCoordinates;
        this.showLastMoveMarker = showLastMoveMarker;
        this.fontFamily = fontFamily;
        draw();
    }

    @Override
    public void setOnBoardChanged(Runnable onBoardChanged) {
        this.onBoardChanged = onBoardChanged;
    }

    @Override
    public void draw() {
        if (palette == null) {
            return;
        }

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        gc.setFill(Color.web(palette.boardBg()));
        double boardArea = getBoardAreaSize();
        double boardOrigin = getBoardOrigin();
        gc.fillRoundRect(boardOrigin, boardOrigin, boardArea, boardArea, 28, 28);

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
            double pos = getGridStart() + i * cell;

            gc.strokeLine(getGridStart(), pos, getGridStart() + cell * (size - 1), pos);
            gc.strokeLine(pos, getGridStart(), pos, getGridStart() + cell * (size - 1));
        }
    }

    private void drawCenterStar(GraphicsContext gc) {
        int size = session.getBoardSize();
        if (size % 2 == 0) {
            return;
        }

        double cell = getCellSize();
        int center = size / 2;
        double x = getGridStart() + center * cell;
        double y = getGridStart() + center * cell;

        gc.setFill(Color.web(palette.boardLine()));
        gc.fillOval(x - 4, y - 4, 8, 8);
    }

    private void drawCoordinates(GraphicsContext gc) {
        int size = session.getBoardSize();
        double cell = getCellSize();

        gc.setFill(Color.web(palette.subtleText()));
        if (fontFamily != null && !fontFamily.isBlank()) {
            gc.setFont(Font.font(fontFamily, 12));
        } else {
            gc.setFont(Font.font(12));
        }

        for (int i = 0; i < size; i++) {
            String colText = String.valueOf((char) ('A' + i));
            String rowText = String.valueOf(i + 1);

            double pos = getGridStart() + i * cell;
            gc.fillText(colText, pos - 4, getGridStart() - 18);
            gc.fillText(colText, pos - 4, getGridStart() + cell * (size - 1) + 24);

            gc.fillText(rowText, getGridStart() - 26, pos + 4);
            gc.fillText(rowText, getGridStart() + cell * (size - 1) + 12, pos + 4);
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

                double centerX = getGridStart() + col * cell;
                double centerY = getGridStart() + row * cell;
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
        double centerX = getGridStart() + lastMove.get().position().col() * cell;
        double centerY = getGridStart() + lastMove.get().position().row() * cell;

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

        int col = (int) Math.round((event.getX() - getGridStart()) / cell);
        int row = (int) Math.round((event.getY() - getGridStart()) / cell);

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
        return (getBoardAreaSize() - getBoardPadding() * 2) / (size - 1);
    }

    private double getBoardAreaSize() {
        return session.getBoardSize() == 3 ? SMALL_BOARD_AREA : CANVAS_SIZE;
    }

    private double getBoardPadding() {
        return session.getBoardSize() == 3 ? SMALL_BOARD_PADDING : PADDING;
    }

    private double getBoardOrigin() {
        return (CANVAS_SIZE - getBoardAreaSize()) / 2.0;
    }

    private double getGridStart() {
        return getBoardOrigin() + getBoardPadding();
    }

    private void notifyBoardChanged() {
        if (onBoardChanged != null) {
            onBoardChanged.run();
        }
    }
}
