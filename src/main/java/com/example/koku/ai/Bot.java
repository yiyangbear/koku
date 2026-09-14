package com.example.koku.ai;

public interface Bot {
    BotMoveResult chooseMove(BotMoveRequest request);
}
