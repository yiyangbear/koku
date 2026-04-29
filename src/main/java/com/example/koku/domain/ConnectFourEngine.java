package com.example.koku.domain;

import com.example.koku.domain.engine.GameEngine;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public class ConnectFourEngine implements GameEngine {
    private static final int ROWS = 6;
    private static final int COLS = 7;
    private static final int WIN_COUNT = 4;
    private static final int[][] DIRECTIONS = {
            {1, 0},
            {0, 1},
            {1, 1},
            {1, -1}
    };

    private final Player[][] grid;
    private final Deque<Move> moveHistory;

    private Player currentPlayer;
    private GameResult result;
    private String latestMessage;
    private int moveCount;

    public ConnectFourEngine() {
        this.grid = new Player[ROWS][COLS];
        this.moveHistory = new ArrayDeque<>();
        this.currentPlayer = Player.BLACK;
        this.result = GameResult.inProgress();
        this.latestMessage = "";
        this.moveCount = 0;
    }

    @Override
    public int getBoardSize() {
        return COLS;
    }

    @Override
    public int getBoardRows() {
        return ROWS;
    }

    @Override
    public int getBoardCols() {
        return COLS;
    }

    @Override
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    @Override
    public GameResult getResult() {
        return result;
    }

    @Override
    public GameStatus getStatus() {
        return result.status();
    }

    @Override
    public String getLatestMessage() {
        return latestMessage;
    }

    @Override
    public Optional<Move> getLastMove() {
        return moveHistory.isEmpty() ? Optional.empty() : Optional.of(moveHistory.peek());
    }

    @Override
    public Player getStoneAt(Position position) {
        if (!isInside(position)) {
            throw new IllegalArgumentException("Position out of board: " + position);
        }
        return grid[position.row()][position.col()];
    }

    @Override
    public boolean isGameOver() {
        return result.isGameOver();
    }

    @Override
    public boolean makeMove(Position position) {
        if (position == null) {
            latestMessage = "Position is null.";
            return false;
        }
        if (isGameOver()) {
            latestMessage = "Game is already over.";
            return false;
        }

        int col = position.col();
        if (col < 0 || col >= COLS) {
            latestMessage = "Column out of board.";
            return false;
        }

        int row = findAvailableRow(col);
        if (row < 0) {
            latestMessage = "Column is full.";
            return false;
        }

        Position resolved = new Position(row, col);
        grid[row][col] = currentPlayer;
        moveHistory.push(new Move(currentPlayer, resolved));
        moveCount++;

        if (hasFourInARow(resolved)) {
            result = currentPlayer == Player.BLACK
                    ? GameResult.blackWin("four")
                    : GameResult.whiteWin("four");
            latestMessage = "Game over.";
            return true;
        }

        if (moveCount >= ROWS * COLS) {
            result = GameResult.draw("draw");
            latestMessage = "Draw.";
            return true;
        }

        currentPlayer = currentPlayer.opposite();
        latestMessage = "Move placed.";
        return true;
    }

    @Override
    public boolean undo() {
        if (moveHistory.isEmpty()) {
            latestMessage = "No move to undo.";
            return false;
        }

        Move move = moveHistory.pop();
        grid[move.position().row()][move.position().col()] = null;
        moveCount--;
        currentPlayer = move.player();
        result = GameResult.inProgress();
        latestMessage = "Move undone.";
        return true;
    }

    @Override
    public void reset() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                grid[row][col] = null;
            }
        }
        moveHistory.clear();
        currentPlayer = Player.BLACK;
        result = GameResult.inProgress();
        latestMessage = "New match started.";
        moveCount = 0;
    }

    @Override
    public void forceTimeoutLoss(Player loser) {
        if (loser == Player.BLACK) {
            result = GameResult.whiteWin("timeout.black");
        } else {
            result = GameResult.blackWin("timeout.white");
        }
        latestMessage = "Timeout.";
    }

    private int findAvailableRow(int col) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (grid[row][col] == null) {
                return row;
            }
        }
        return -1;
    }

    private boolean hasFourInARow(Position position) {
        Player player = grid[position.row()][position.col()];
        for (int[] direction : DIRECTIONS) {
            int count = 1;
            count += countDirection(position, player, direction[0], direction[1]);
            count += countDirection(position, player, -direction[0], -direction[1]);
            if (count >= WIN_COUNT) {
                return true;
            }
        }
        return false;
    }

    private int countDirection(Position start, Player player, int dr, int dc) {
        int count = 0;
        int row = start.row() + dr;
        int col = start.col() + dc;

        while (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
            if (grid[row][col] != player) {
                break;
            }
            count++;
            row += dr;
            col += dc;
        }
        return count;
    }

    private boolean isInside(Position position) {
        return position.row() >= 0
                && position.row() < ROWS
                && position.col() >= 0
                && position.col() < COLS;
    }
}
