package com.example.koku.ai;

import com.example.koku.domain.ConnectFourEngine;
import com.example.koku.domain.Move;
import com.example.koku.domain.Player;
import com.example.koku.domain.Position;
import com.example.koku.domain.TicTacToeEngine;
import com.example.koku.game.GameRegistry;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BotBehaviorTest {

    @Test
    void easyBotReturnsAnEmptyCell() {
        TicTacToeEngine engine = new TicTacToeEngine();
        engine.makeMove(new Position(1, 1));

        BotMoveResult result = new RandomBot(new Random(7)).chooseMove(
                new BotMoveRequest(engine, Player.WHITE, Player.BLACK, GameRegistry.ticTacToe())
        );

        Move move = result.move().orElseThrow();
        assertEquals(Player.WHITE, move.player());
        assertNull(engine.getStoneAt(move.position()));
    }

    @Test
    void hardTicTacToeBotTakesAnImmediateWin() {
        TicTacToeEngine engine = new TicTacToeEngine();
        play(engine, 0, 0, 1, 0, 0, 1, 1, 1);

        Move move = hardTicTacToeMove(engine, Player.BLACK);

        assertEquals(new Position(0, 2), move.position());
    }

    @Test
    void hardTicTacToeBotBlocksAnImmediateLoss() {
        TicTacToeEngine engine = new TicTacToeEngine();
        play(engine, 1, 1, 0, 0, 2, 2, 0, 1);

        Move move = hardTicTacToeMove(engine, Player.BLACK);

        assertEquals(new Position(0, 2), move.position());
    }

    @Test
    void hardConnectFourBotTakesAnImmediateWin() {
        ConnectFourEngine engine = new ConnectFourEngine();
        play(engine, 0, 6, 1, 6, 2, 5);

        BotMoveResult result = new MinimaxBot().chooseMove(
                new BotMoveRequest(engine, Player.BLACK, Player.WHITE, GameRegistry.connectFour())
        );

        Move move = result.move().orElseThrow();
        assertEquals(3, move.position().col());
        assertEquals(5, move.position().row());
        assertTrue(engine.makeMove(move.position()));
        assertTrue(engine.isGameOver());
    }

    private Move hardTicTacToeMove(TicTacToeEngine engine, Player botPlayer) {
        BotMoveResult result = new MinimaxBot().chooseMove(
                new BotMoveRequest(engine, botPlayer, botPlayer.opposite(), GameRegistry.ticTacToe())
        );
        Move move = result.move().orElseThrow();
        assertNotNull(move.position());
        return move;
    }

    private void play(TicTacToeEngine engine, int... coordinates) {
        for (int index = 0; index < coordinates.length; index += 2) {
            assertTrue(engine.makeMove(new Position(coordinates[index], coordinates[index + 1])));
        }
    }

    private void play(ConnectFourEngine engine, int... columns) {
        for (int column : columns) {
            assertTrue(engine.makeMove(new Position(0, column)));
        }
    }
}
