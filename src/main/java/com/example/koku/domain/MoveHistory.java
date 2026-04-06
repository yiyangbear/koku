package com.example.koku.domain;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

public class MoveHistory {
    private final Deque<Move> moves = new ArrayDeque<>();

    public void push(Move move) {
        if (move == null) {
            throw new IllegalArgumentException("Move cannot be null.");
        }
        moves.push(move);
    }

    public Optional<Move> pop() {
        if (moves.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(moves.pop());
    }

    public Optional<Move> peek() {
        if (moves.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(moves.peek());
    }

    public boolean isEmpty() {
        return moves.isEmpty();
    }

    public int size() {
        return moves.size();
    }

    public void clear() {
        moves.clear();
    }

    public List<Move> asListOldestFirst() {
        List<Move> list = new ArrayList<>(moves);
        java.util.Collections.reverse(list);
        return list;
    }
}