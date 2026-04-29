package com.example.koku.domain;

import com.example.koku.domain.engine.GameEngine;

import java.util.Optional;

public class SixInRowEngine implements GameEngine {
    private static final int WIN_COUNT = 6;
    private static final int[][] DIRECTIONS = {
            {1, 0},
            {0, 1},
            {1, 1},
            {1, -1}
    };

    private final Board board;
    private final MoveHistory moveHistory;

    private Player currentPlayer;
    private GameResult result;
    private String latestMessage;
    private int stonesThisTurn;
    private boolean firstMove;

    public SixInRowEngine(int boardSize) {
        this.board = new Board(boardSize);
        this.moveHistory = new MoveHistory();
        this.currentPlayer = Player.BLACK;
        this.result = GameResult.inProgress();
        this.latestMessage = "";
        this.stonesThisTurn = 0;
        this.firstMove = true;
    }

    @Override
    public int getBoardSize() {
        return board.getSize();
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
        return moveHistory.peek();
    }

    @Override
    public Player getStoneAt(Position position) {
        return board.getStone(position);
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
        if (!board.isInside(position)) {
            latestMessage = "Position out of board.";
            return false;
        }
        if (!board.isEmpty(position)) {
            latestMessage = "Position occupied.";
            return false;
        }

        board.placeStone(currentPlayer, position);
        moveHistory.push(new Move(currentPlayer, position));
        stonesThisTurn++;

        if (hasSixInARow(position)) {
            result = currentPlayer == Player.BLACK
                    ? GameResult.blackWin("six")
                    : GameResult.whiteWin("six");
            latestMessage = "Game over.";
            return true;
        }

        if (board.isFull()) {
            result = GameResult.draw("draw");
            latestMessage = "Draw.";
            return true;
        }

        int requiredStones = firstMove ? 1 : 2;
        if (stonesThisTurn >= requiredStones) {
            currentPlayer = currentPlayer.opposite();
            stonesThisTurn = 0;
            firstMove = false;
        }

        latestMessage = "Move placed.";
        return true;
    }

    @Override
    public boolean undo() {
        Optional<Move> lastMove = moveHistory.pop();
        if (lastMove.isEmpty()) {
            latestMessage = "No move to undo.";
            return false;
        }

        Move move = lastMove.get();
        board.removeStone(move.position());
        currentPlayer = move.player();
        result = GameResult.inProgress();
        recalculateTurnState();
        latestMessage = "Move undone.";
        return true;
    }

    @Override
    public void reset() {
        board.clear();
        moveHistory.clear();
        currentPlayer = Player.BLACK;
        result = GameResult.inProgress();
        latestMessage = "New match started.";
        stonesThisTurn = 0;
        firstMove = true;
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

    private boolean hasSixInARow(Position position) {
        Player player = board.getStone(position);
        if (player == null) {
            return false;
        }

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

    private void recalculateTurnState() {
        int moveCount = board.getMoveCount();
        if (moveCount == 0) {
            firstMove = true;
            stonesThisTurn = 0;
            currentPlayer = Player.BLACK;
            return;
        }

        firstMove = false;
        int afterOpening = moveCount - 1;
        currentPlayer = afterOpening % 4 < 2 ? Player.WHITE : Player.BLACK;
        stonesThisTurn = afterOpening % 2;
    }
}
