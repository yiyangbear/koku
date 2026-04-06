package com.example.koku.domain;

import java.util.Optional;

public class GomokuEngine {
    private final Board board;
    private final MoveHistory moveHistory;
    private final WinChecker winChecker;

    private Player currentPlayer;
    private GameResult result;
    private String latestMessage;

    public GomokuEngine(int boardSize) {
        this.board = new Board(boardSize);
        this.moveHistory = new MoveHistory();
        this.winChecker = new WinChecker();
        this.currentPlayer = Player.BLACK;
        this.result = GameResult.inProgress();
        this.latestMessage = "";
    }

    public Board getBoard() {
        return board;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public GameResult getResult() {
        return result;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    public Optional<Move> getLastMove() {
        return moveHistory.peek();
    }

    public boolean isGameOver() {
        return result.isGameOver();
    }

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
        Move move = new Move(currentPlayer, position);
        moveHistory.push(move);

        if (winChecker.hasFiveInARow(board, position)) {
            result = currentPlayer == Player.BLACK
                    ? GameResult.blackWin("five")
                    : GameResult.whiteWin("five");
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

    public void reset() {
        board.clear();
        moveHistory.clear();
        currentPlayer = Player.BLACK;
        result = GameResult.inProgress();
        latestMessage = "New match started.";
    }

    public void forceTimeoutLoss(Player loser) {
        if (loser == Player.BLACK) {
            result = GameResult.whiteWin("timeout.black");
        } else {
            result = GameResult.blackWin("timeout.white");
        }
        latestMessage = "Timeout.";
    }
}