package com.example.koku.ui.boards;

import com.example.koku.domain.Move;
import com.example.koku.domain.Player;
import com.example.koku.service.GameSession;
import com.example.koku.service.ThemeService;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.Interpolator;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.Optional;

public class ConnectFourBoardView extends Canvas implements GameBoardView {
    private static final double INITIAL_CANVAS_SIZE = 720;

    private final GameSession session;

    private ThemeService.Palette palette;
    private boolean showCoordinates;
    private boolean showLastMoveMarker;
    private String fontFamily;
    private Runnable onBoardChanged;
    private Runnable onMoveSettled;

    private Integer hoverColumn;
    private Timeline fallAnimation;
    private FallingStone fallingStone;

    public ConnectFourBoardView(GameSession session) {
        super(INITIAL_CANVAS_SIZE, INITIAL_CANVAS_SIZE);
        this.session = session;
        widthProperty().addListener((obs, oldVal, newVal) -> draw());
        heightProperty().addListener((obs, oldVal, newVal) -> draw());
        setOnMouseMoved(this::handleMouseMoved);
        setOnMouseExited(event -> {
            hoverColumn = null;
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
    public void draw() {
        if (palette == null) {
            return;
        }

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        double boardX = getBoardX();
        double boardY = getBoardY();

        gc.setFill(Color.web(palette.boardBg()));
        gc.fillRoundRect(boardX, boardY, getBoardWidth(), getBoardHeight(), getCornerRadius(), getCornerRadius());

        drawGrid(gc);
        if (showCoordinates) {
            drawCoordinates(gc);
        }
        drawHoverColumn(gc);
        drawStones(gc);
        if (showLastMoveMarker) {
            drawLastMoveMarker(gc);
        }
        drawFallingStone(gc);
    }

    @Override
    public void setOnBoardChanged(Runnable onBoardChanged) {
        this.onBoardChanged = onBoardChanged;
    }

    public void setOnMoveSettled(Runnable onMoveSettled) {
        this.onMoveSettled = onMoveSettled;
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(Color.web(palette.boardLine()));
        gc.setLineWidth(Math.max(0.9, getBoardWidth() / 540.0));

        for (int col = 0; col < session.getBoardCols(); col++) {
            double x = getGridStartX() + col * getCellWidth();
            gc.strokeLine(x, getGridStartY(), x, getGridStartY() + getCellHeight() * (session.getBoardRows() - 1));
        }

        for (int row = 0; row < session.getBoardRows(); row++) {
            double y = getGridStartY() + row * getCellHeight();
            gc.strokeLine(getGridStartX(), y, getGridStartX() + getCellWidth() * (session.getBoardCols() - 1), y);
        }
    }

    private void drawCoordinates(GraphicsContext gc) {
        gc.setFill(Color.web(palette.subtleText()));
        gc.setFont(resolveFont(12));

        for (int col = 0; col < session.getBoardCols(); col++) {
            double x = getGridStartX() + col * getCellWidth();
            gc.fillText(String.valueOf((char) ('A' + col)), x - 4, getGridStartY() - 18);
        }

        for (int row = 0; row < session.getBoardRows(); row++) {
            double y = getGridStartY() + row * getCellHeight();
            gc.fillText(String.valueOf(row + 1), getGridStartX() - 26, y + 4);
        }
    }

    private void drawHoverColumn(GraphicsContext gc) {
        if (hoverColumn == null || session.isGameOver()) {
            return;
        }
        double x = getGridStartX() + hoverColumn * getCellWidth() - getCellWidth() / 2.0;
        gc.setFill(Color.web(palette.accent(), 0.14));
        gc.fillRoundRect(x, getGridStartY() - getCellHeight() / 2.0,
                getCellWidth(), getCellHeight() * session.getBoardRows(), 16, 16);
    }

    private void drawStones(GraphicsContext gc) {
        double stoneSize = Math.min(getCellWidth(), getCellHeight()) * 0.78;

        for (int row = 0; row < session.getBoardRows(); row++) {
            for (int col = 0; col < session.getBoardCols(); col++) {
                if (isAnimatingLastStone(row, col)) {
                    continue;
                }
                Player stone = session.getStoneAt(row, col);
                if (stone == null) {
                    continue;
                }
                drawStone(gc, stone, row, col, stoneSize);
            }
        }
    }

    private void drawLastMoveMarker(GraphicsContext gc) {
        Optional<Move> lastMove = session.getLastMove();
        if (lastMove.isEmpty() || fallingStone != null) {
            return;
        }

        double centerX = getGridStartX() + lastMove.get().position().col() * getCellWidth();
        double centerY = getGridStartY() + lastMove.get().position().row() * getCellHeight();
        gc.setFill(Color.web(palette.accent()));
        gc.fillOval(centerX - 4, centerY - 4, 8, 8);
    }

    private void drawFallingStone(GraphicsContext gc) {
        if (fallingStone == null) {
            return;
        }
        double stoneSize = Math.min(getCellWidth(), getCellHeight()) * 0.78;
        double centerX = getGridStartX() + fallingStone.col() * getCellWidth();
        double x = centerX - stoneSize / 2.0;
        double y = fallingStone.currentY() - stoneSize / 2.0;
        fillStone(gc, fallingStone.player(), x, y, stoneSize);
    }

    private void handleMouseMoved(MouseEvent event) {
        hoverColumn = resolveColumn(event.getX());
        draw();
    }

    private void handleMouseClicked(MouseEvent event) {
        if (session.isGameOver() || fallingStone != null) {
            notifyBoardChanged();
            return;
        }

        Integer col = resolveColumn(event.getX());
        if (col == null) {
            notifyBoardChanged();
            return;
        }

        Player placedPlayer = session.getCurrentPlayer();
        boolean success = session.makeMove(0, col);
        if (!success) {
            notifyBoardChanged();
            return;
        }

        Optional<Move> lastMove = session.getLastMove();
        if (lastMove.isPresent()) {
            startFallAnimation(placedPlayer, lastMove.get().position());
            return;
        }

        draw();
        notifyBoardChanged();
    }

    private void startFallAnimation(Player player, com.example.koku.domain.Position target) {
        if (fallAnimation != null) {
            fallAnimation.stop();
        }

        double startY = getGridStartY() - getCellHeight() / 2.0;
        double endY = getGridStartY() + target.row() * getCellHeight();
        fallingStone = new FallingStone(player, target.col(), startY);
        fallingStone.currentYProperty().addListener((obs, oldVal, newVal) -> draw());
        draw();

        fallAnimation = new Timeline(
                new KeyFrame(Duration.millis(220 + target.row() * 40.0),
                        new KeyValue(fallingStone.currentYProperty(), endY, Interpolator.EASE_OUT))
        );
        fallAnimation.setOnFinished(event -> {
            fallingStone = null;
            draw();
            hoverColumn = null;
            notifyBoardChanged();
            notifyMoveSettled();
        });
        fallAnimation.play();
    }

    private Integer resolveColumn(double x) {
        double left = getGridStartX() - getCellWidth() / 2.0;
        double right = getGridStartX() + getCellWidth() * (session.getBoardCols() - 1) + getCellWidth() / 2.0;
        if (x < left || x > right) {
            return null;
        }
        int col = (int) Math.round((x - getGridStartX()) / getCellWidth());
        return col >= 0 && col < session.getBoardCols() ? col : null;
    }

    private void drawStone(GraphicsContext gc, Player stone, int row, int col, double stoneSize) {
        double centerX = getGridStartX() + col * getCellWidth();
        double centerY = getGridStartY() + row * getCellHeight();
        double x = centerX - stoneSize / 2.0;
        double y = centerY - stoneSize / 2.0;
        fillStone(gc, stone, x, y, stoneSize);
    }

    private void fillStone(GraphicsContext gc, Player stone, double x, double y, double stoneSize) {
        if (stone == Player.BLACK) {
            gc.setFill(Color.web(palette.blackStone()));
            gc.fillOval(x, y, stoneSize, stoneSize);
            if (palette.darkMode() && palette.blackStoneBorder() != null) {
                gc.setStroke(Color.web(palette.blackStoneBorder()));
                gc.setLineWidth(1.2);
                gc.strokeOval(x, y, stoneSize, stoneSize);
            }
            return;
        }

        gc.setFill(Color.web(palette.whiteStone()));
        gc.fillOval(x, y, stoneSize, stoneSize);
        gc.setStroke(Color.web(palette.whiteStoneBorder()));
        gc.setLineWidth(1.0);
        gc.strokeOval(x, y, stoneSize, stoneSize);
    }

    private boolean isAnimatingLastStone(int row, int col) {
        return fallingStone != null
                && session.getLastMove().isPresent()
                && session.getLastMove().get().position().row() == row
                && session.getLastMove().get().position().col() == col;
    }

    private Font resolveFont(double size) {
        if (fontFamily != null && !fontFamily.isBlank()) {
            return Font.font(fontFamily, size);
        }
        return Font.font(size);
    }

    private double getBoardX() {
        return (getWidth() - getBoardWidth()) / 2.0;
    }

    private double getBoardY() {
        return (getHeight() - getBoardHeight()) / 2.0;
    }

    private double getGridStartX() {
        return getBoardX() + getBoardPadding();
    }

    private double getGridStartY() {
        return getBoardY() + getBoardPadding();
    }

    private double getCellWidth() {
        return getCellSize();
    }

    private double getCellHeight() {
        return getCellSize();
    }

    private double getCanvasSize() {
        return Math.max(260, Math.min(getWidth(), getHeight()));
    }

    private double getCellSize() {
        double canvasSize = getCanvasSize();
        return Math.min(
                canvasSize * 0.74 / (session.getBoardCols() - 1),
                canvasSize * 0.64 / (session.getBoardRows() - 1)
        );
    }

    private double getBoardPadding() {
        return Math.max(26, getCellSize() * 0.62);
    }

    private double getBoardWidth() {
        return getCellSize() * (session.getBoardCols() - 1) + getBoardPadding() * 2;
    }

    private double getBoardHeight() {
        return getCellSize() * (session.getBoardRows() - 1) + getBoardPadding() * 2;
    }

    private double getCornerRadius() {
        return Math.max(18, Math.min(28, getBoardWidth() * 0.05));
    }

    private void notifyBoardChanged() {
        if (onBoardChanged != null) {
            onBoardChanged.run();
        }
    }

    private void notifyMoveSettled() {
        if (onMoveSettled != null) {
            onMoveSettled.run();
        }
    }

    private static final class FallingStone {
        private final Player player;
        private final int col;
        private final javafx.beans.property.DoubleProperty currentY;

        private FallingStone(Player player, int col, double initialY) {
            this.player = player;
            this.col = col;
            this.currentY = new javafx.beans.property.SimpleDoubleProperty(initialY);
        }

        public Player player() {
            return player;
        }

        public int col() {
            return col;
        }

        public double currentY() {
            return currentY.get();
        }

        public javafx.beans.property.DoubleProperty currentYProperty() {
            return currentY;
        }
    }
}
