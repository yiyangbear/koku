package com.example.koku.domain;

public record GameResult(GameStatus status, Player winner, String reason) {

    public static GameResult inProgress() {
        return new GameResult(GameStatus.IN_PROGRESS, null, "");
    }

    public static GameResult blackWin(String reason) {
        return new GameResult(GameStatus.BLACK_WIN, Player.BLACK, reason);
    }

    public static GameResult whiteWin(String reason) {
        return new GameResult(GameStatus.WHITE_WIN, Player.WHITE, reason);
    }

    public static GameResult draw(String reason) {
        return new GameResult(GameStatus.DRAW, null, reason);
    }

    public boolean isGameOver() {
        return status != GameStatus.IN_PROGRESS;
    }
}