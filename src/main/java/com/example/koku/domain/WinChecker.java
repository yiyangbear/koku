package com.example.koku.domain;

public class WinChecker {
    private static final int[][] DIRECTIONS = {
            {1, 0},
            {0, 1},
            {1, 1},
            {1, -1}
    };

    public boolean hasFiveInARow(Board board, Position position) {
        Player player = board.getStone(position);
        if (player == null) {
            return false;
        }

        for (int[] direction : DIRECTIONS) {
            int count = 1;
            count += countDirection(board, position, player, direction[0], direction[1]);
            count += countDirection(board, position, player, -direction[0], -direction[1]);
            if (count >= 5) {
                return true;
            }
        }
        return false;
    }

    private int countDirection(Board board, Position start, Player player, int dr, int dc) {
        int count = 0;
        int row = start.row() + dr;
        int col = start.col() + dc;

        while (row >= 0 && row < board.getSize() && col >= 0 && col < board.getSize()) {
            Position current = new Position(row, col);
            if (board.getStone(current) != player) {
                break;
            }
            count++;
            row += dr;
            col += dc;
        }
        return count;
    }
}