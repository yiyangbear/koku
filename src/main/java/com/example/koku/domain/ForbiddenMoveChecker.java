package com.example.koku.domain;

public class ForbiddenMoveChecker {
    private static final int[][] DIRECTIONS = {
            {1, 0},
            {0, 1},
            {1, 1},
            {1, -1}
    };

    public boolean isForbidden(Board board, Position position) {
        if (isOverline(board, position)) {
            return true;
        }

        int fourCount = 0;
        int threeCount = 0;

        for (int[] dir : DIRECTIONS) {
            if (hasOpenFourInDirection(board, dir[0], dir[1])) {
                fourCount++;
            }
            if (hasOpenThreeInDirection(board, dir[0], dir[1])) {
                threeCount++;
            }
        }

        return fourCount >= 2 || threeCount >= 2;
    }

    private boolean isOverline(Board board, Position position) {
        for (int[] dir : DIRECTIONS) {
            int count = 1;
            count += countContinuous(board, position, dir[0], dir[1]);
            count += countContinuous(board, position, -dir[0], -dir[1]);
            if (count >= 6) {
                return true;
            }
        }
        return false;
    }

    private int countContinuous(Board board, Position start, int dr, int dc) {
        int count = 0;
        int row = start.row() + dr;
        int col = start.col() + dc;

        while (row >= 0 && row < board.getSize() && col >= 0 && col < board.getSize()) {
            Position current = new Position(row, col);
            if (board.getStone(current) != Player.BLACK) {
                break;
            }
            count++;
            row += dr;
            col += dc;
        }
        return count;
    }

    private boolean hasOpenFourInDirection(Board board, int dr, int dc) {
        for (String line : buildLines(board, dr, dc)) {
            if (containsOpenFour(line)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasOpenThreeInDirection(Board board, int dr, int dc) {
        for (String line : buildLines(board, dr, dc)) {
            if (containsOpenThree(line)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsOpenFour(String line) {
        return containsPattern(line, ".BBBB.")
                || containsPattern(line, ".BBB.B.")
                || containsPattern(line, ".BB.BB.")
                || containsPattern(line, ".B.BBB.");
    }

    private boolean containsOpenThree(String line) {
        return containsPattern(line, ".BBB.")
                || containsPattern(line, ".BB.B.")
                || containsPattern(line, ".B.BB.");
    }

    private boolean containsPattern(String line, String pattern) {
        return line.contains(pattern);
    }

    private java.util.List<String> buildLines(Board board, int dr, int dc) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        int size = board.getSize();

        if (dr == 0 && dc == 1) {
            for (int row = 0; row < size; row++) {
                lines.add(buildLineFrom(board, row, 0, dr, dc));
            }
            return lines;
        }

        if (dr == 1 && dc == 0) {
            for (int col = 0; col < size; col++) {
                lines.add(buildLineFrom(board, 0, col, dr, dc));
            }
            return lines;
        }

        if (dr == 1 && dc == 1) {
            for (int col = 0; col < size; col++) {
                lines.add(buildLineFrom(board, 0, col, dr, dc));
            }
            for (int row = 1; row < size; row++) {
                lines.add(buildLineFrom(board, row, 0, dr, dc));
            }
            return lines;
        }

        for (int col = 0; col < size; col++) {
            lines.add(buildLineFrom(board, 0, col, dr, dc));
        }
        for (int row = 1; row < size; row++) {
            lines.add(buildLineFrom(board, row, size - 1, dr, dc));
        }
        return lines;
    }

    private String buildLineFrom(Board board, int startRow, int startCol, int dr, int dc) {
        StringBuilder line = new StringBuilder();
        int size = board.getSize();
        int row = startRow;
        int col = startCol;
        while (row >= 0 && row < size && col >= 0 && col < size) {
            Position current = new Position(row, col);
            Player stone = board.getStone(current);
            if (stone == null) {
                line.append('.');
            } else if (stone == Player.BLACK) {
                line.append('B');
            } else {
                line.append('W');
            }
            row += dr;
            col += dc;
        }
        return line.toString();
    }

    private char[] buildLine(Board board, Position position, int dr, int dc) {
        char[] line = new char[9];
        for (int i = -4; i <= 4; i++) {
            int row = position.row() + dr * i;
            int col = position.col() + dc * i;
            int index = i + 4;
            if (row < 0 || row >= board.getSize() || col < 0 || col >= board.getSize()) {
                line[index] = 'W';
                continue;
            }
            Position current = new Position(row, col);
            Player stone = board.getStone(current);
            if (stone == null) {
                line[index] = '.';
            } else if (stone == Player.BLACK) {
                line[index] = 'B';
            } else {
                line[index] = 'W';
            }
        }
        return line;
    }
}
