package com.example.koku.domain;

public enum Player {
    BLACK,
    WHITE;

    public Player opposite() {
        return this == BLACK ? WHITE : BLACK;
    }
}