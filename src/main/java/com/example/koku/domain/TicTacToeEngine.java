package com.example.koku.domain;

import com.example.koku.domain.engine.GameEngine;

import java.util.Optional;

public class TicTacToeEngine implements GameEngine {
    private static final int BOARD_SIZE = 3;

    private final Board board;
    private final MoveHistory moveHistory;

    private Player currentPlayer;
    private GameResult result;
    private String latestMessage;

    public TicTacToeEngine() {
        this.board = new Board(BOARD_SIZE);
        this.moveHistory = new MoveHistory();
        this.currentPlayer = Player.BLACK;
        this.result = GameResult.inProgress();
        this.latestMessage = "";
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

        if (hasThreeInARow(position)) {
            result = currentPlayer == Player.BLACK
                    ? GameResult.blackWin("three")
                    : GameResult.whiteWin("three");
            latestMessage = "Game over.";
            return true;
        }

        if (board.isFull()) {
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
        Optional<Move> lastMove = moveHistory.pop();
        if (lastMove.isEmpty()) {
            latestMessage = "No move to undo.";
            return false;
        }

        Move move = lastMove.get();
        board.removeStone(move.position());
        currentPlayer = move.player();
        result = GameResult.inProgress();
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

    private boolean hasThreeInARow(Position position) {
        Player player = board.getStone(position);
        int row = position.row();
        int col = position.col();

        return hasLine(player, new Position(row, 0), new Position(row, 1), new Position(row, 2))
                || hasLine(player, new Position(0, col), new Position(1, col), new Position(2, col))
                || hasLine(player, new Position(0, 0), new Position(1, 1), new Position(2, 2))
                || hasLine(player, new Position(0, 2), new Position(1, 1), new Position(2, 0));
    }

    private boolean hasLine(Player player, Position a, Position b, Position c) {
        return board.getStone(a) == player
                && board.getStone(b) == player
                && board.getStone(c) == player;
    }
}
