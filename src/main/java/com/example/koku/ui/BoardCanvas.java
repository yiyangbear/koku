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
    private Integer hoverRow;
    private Integer hoverCol;

    private Runnable onBoardChanged;

    private static final double INITIAL_CANVAS_SIZE = 720;

    public BoardCanvas(GameSession session) {
        super(INITIAL_CANVAS_SIZE, INITIAL_CANVAS_SIZE);
        this.session = session;
        widthProperty().addListener((obs, oldVal, newVal) -> draw());
        heightProperty().addListener((obs, oldVal, newVal) -> draw());
        setOnMouseMoved(this::handleMouseMoved);
        setOnMouseExited(event -> {
            hoverRow = null;
            hoverCol = null;
            draw();
        });
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
        gc.fillRoundRect(getBoardOriginX(), getBoardOriginY(), boardArea, boardArea,
                getCornerRadius(), getCornerRadius());

        drawGrid(gc);
        drawCenterStar(gc);

        if (showCoordinates) {
            drawCoordinates(gc);
        }

        drawHoverStone(gc);
        drawStones(gc);

        if (showLastMoveMarker) {
            drawLastMoveMarker(gc);
        }
    }

    private void drawGrid(GraphicsContext gc) {
        int size = session.getBoardSize();
        double cell = getCellSize();

        gc.setStroke(Color.web(palette.boardLine()));
        gc.setLineWidth(Math.max(0.85, getBoardAreaSize() / 720.0));

        for (int i = 0; i < size; i++) {
            double x = getGridStartX() + i * cell;
            double y = getGridStartY() + i * cell;

            gc.strokeLine(getGridStartX(), y, getGridStartX() + cell * (size - 1), y);
            gc.strokeLine(x, getGridStartY(), x, getGridStartY() + cell * (size - 1));
        }
    }

    private void drawCenterStar(GraphicsContext gc) {
        int size = session.getBoardSize();
        if (size % 2 == 0) {
            return;
        }

        double cell = getCellSize();
        int center = size / 2;
        double x = getGridStartX() + center * cell;
        double y = getGridStartY() + center * cell;
        double radius = Math.max(3, Math.min(4.5, cell * 0.11));

        gc.setFill(Color.web(palette.boardLine()));
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
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

            double x = getGridStartX() + i * cell;
            double y = getGridStartY() + i * cell;
            gc.fillText(colText, x - 4, getGridStartY() - Math.max(14, cell * 0.35));
            gc.fillText(colText, x - 4, getGridStartY() + cell * (size - 1) + Math.max(18, cell * 0.45));

            gc.fillText(rowText, getGridStartX() - Math.max(22, cell * 0.55), y + 4);
            gc.fillText(rowText, getGridStartX() + cell * (size - 1) + Math.max(10, cell * 0.25), y + 4);
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

                double centerX = getGridStartX() + col * cell;
                double centerY = getGridStartY() + row * cell;
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

    private void drawHoverStone(GraphicsContext gc) {
        if (session.isGameOver() || hoverRow == null || hoverCol == null) {
            return;
        }
        if (session.getStoneAt(hoverRow, hoverCol) != null) {
            return;
        }

        double cell = getCellSize();
        double stoneSize = cell * 0.78;
        double centerX = getGridStartX() + hoverCol * cell;
        double centerY = getGridStartY() + hoverRow * cell;
        double x = centerX - stoneSize / 2.0;
        double y = centerY - stoneSize / 2.0;

        if (session.getCurrentPlayer() == Player.BLACK) {
            gc.setFill(Color.web(palette.blackStone(), 0.35));
            gc.fillOval(x, y, stoneSize, stoneSize);
            if (palette.darkMode() && palette.blackStoneBorder() != null) {
                gc.setStroke(Color.web(palette.blackStoneBorder(), 0.45));
                gc.setLineWidth(1.2);
                gc.strokeOval(x, y, stoneSize, stoneSize);
            }
        } else {
            gc.setFill(Color.web(palette.whiteStone(), 0.55));
            gc.fillOval(x, y, stoneSize, stoneSize);
            gc.setStroke(Color.web(palette.whiteStoneBorder(), 0.55));
            gc.setLineWidth(1.0);
            gc.strokeOval(x, y, stoneSize, stoneSize);
        }
    }

    private void drawLastMoveMarker(GraphicsContext gc) {
        Optional<Move> lastMove = session.getLastMove();
        if (lastMove.isEmpty()) {
            return;
        }

        double cell = getCellSize();
        double centerX = getGridStartX() + lastMove.get().position().col() * cell;
        double centerY = getGridStartY() + lastMove.get().position().row() * cell;
        double markerRadius = Math.max(3, Math.min(4, cell * 0.1));

        gc.setFill(Color.web(palette.accent()));
        gc.fillOval(centerX - markerRadius, centerY - markerRadius, markerRadius * 2, markerRadius * 2);
    }

    private void handleMouseMoved(MouseEvent event) {
        int size = session.getBoardSize();
        double cell = getCellSize();

        int col = (int) Math.round((event.getX() - getGridStartX()) / cell);
        int row = (int) Math.round((event.getY() - getGridStartY()) / cell);

        if (row < 0 || row >= size || col < 0 || col >= size) {
            if (hoverRow != null || hoverCol != null) {
                hoverRow = null;
                hoverCol = null;
                draw();
            }
            return;
        }

        if (!Integer.valueOf(row).equals(hoverRow) || !Integer.valueOf(col).equals(hoverCol)) {
            hoverRow = row;
            hoverCol = col;
            draw();
        }
    }

    private void handleMouseClicked(MouseEvent event) {
        if (session.isGameOver()) {
            notifyBoardChanged();
            return;
        }

        int size = session.getBoardSize();
        double cell = getCellSize();

        int col = (int) Math.round((event.getX() - getGridStartX()) / cell);
        int row = (int) Math.round((event.getY() - getGridStartY()) / cell);

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
        double canvasSize = Math.max(260, Math.min(getWidth(), getHeight()));
        return session.getBoardSize() == 3 ? canvasSize * 0.62 : canvasSize;
    }

    private double getBoardPadding() {
        double boardArea = getBoardAreaSize();
        return session.getBoardSize() == 3
                ? Math.max(54, boardArea * 0.24)
                : Math.max(24, boardArea * 0.072);
    }

    private double getBoardOriginX() {
        return (getWidth() - getBoardAreaSize()) / 2.0;
    }

    private double getBoardOriginY() {
        return (getHeight() - getBoardAreaSize()) / 2.0;
    }

    private double getGridStartX() {
        return getBoardOriginX() + getBoardPadding();
    }

    private double getGridStartY() {
        return getBoardOriginY() + getBoardPadding();
    }

    private double getCornerRadius() {
        return Math.max(18, Math.min(28, getBoardAreaSize() * 0.04));
    }

    private void notifyBoardChanged() {
        if (onBoardChanged != null) {
            onBoardChanged.run();
        }
    }
}
