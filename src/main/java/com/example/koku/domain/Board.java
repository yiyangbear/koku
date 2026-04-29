package com.example.koku.domain;

import java.util.Arrays;

public class Board {
    private final int size;
    private final Player[][] grid;
    private int moveCount;

    public Board(int size) {
        if (size < 3) {
            throw new IllegalArgumentException("Board size must be at least 3.");
        }
        this.size = size;
        this.grid = new Player[size][size];
        this.moveCount = 0;
    }

    public int getSize() {
        return size;
    }

    public boolean isInside(Position position) {
        return position != null
                && position.row() >= 0
                && position.row() < size
                && position.col() >= 0
                && position.col() < size;
    }

    public boolean isEmpty(Position position) {
        validateInside(position);
        return grid[position.row()][position.col()] == null;
    }

    public Player getStone(Position position) {
        validateInside(position);
        return grid[position.row()][position.col()];
    }

    public void placeStone(Player player, Position position) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null.");
        }
        validateInside(position);
        if (!isEmpty(position)) {
            throw new IllegalStateException("Position already occupied.");
        }
        grid[position.row()][position.col()] = player;
        moveCount++;
    }

    public void removeStone(Position position) {
        validateInside(position);
        if (grid[position.row()][position.col()] != null) {
            grid[position.row()][position.col()] = null;
            moveCount--;
        }
    }

    public boolean isFull() {
        return moveCount >= size * size;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void clear() {
        for (Player[] row : grid) {
            Arrays.fill(row, null);
        }
        moveCount = 0;
    }

    private void validateInside(Position position) {
        if (!isInside(position)) {
            throw new IllegalArgumentException("Position out of board: " + position);
        }
    }
}
