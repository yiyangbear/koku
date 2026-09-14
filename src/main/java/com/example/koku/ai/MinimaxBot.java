package com.example.koku.ai;

import com.example.koku.ai.evaluator.GomokuEvaluator;
import com.example.koku.domain.Board;
import com.example.koku.domain.ForbiddenMoveChecker;
import com.example.koku.domain.GameResult;
import com.example.koku.domain.GameStatus;
import com.example.koku.domain.GomokuEngine;
import com.example.koku.domain.Move;
import com.example.koku.domain.Player;
import com.example.koku.domain.Position;
import com.example.koku.domain.engine.GameEngine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class MinimaxBot implements Bot {
    private static final int TIC_TAC_TOE_SIZE = 3;
    private static final int CONNECT_FOUR_ROWS = 6;
    private static final int CONNECT_FOUR_COLS = 7;
    private static final int CONNECT_FOUR_WIN_COUNT = 4;
    // TODO: Make minimax depth configurable and run deeper searches on a background Task.
    private static final int CONNECT_FOUR_SEARCH_DEPTH = 5;
    // TODO: Make Gomoku search depth configurable, improve move ordering, add a transposition table,
    // and run deeper searches on a background Task/thread.
    private static final int GOMOKU_SEARCH_DEPTH = 2;
    private static final int GOMOKU_TOP_CANDIDATES = 18;
    private static final int GOMOKU_TARGET_LENGTH = 5;
    private static final int GOMOKU_TACTICAL_KEEP_SCORE = 150_000;
    private static final int WIN_SCORE = 1_000_000;
    private static final int[][] CONNECT_FOUR_DIRECTIONS = {
            {1, 0},
            {0, 1},
            {1, 1},
            {1, -1}
    };

    private final HeuristicBot fallback;
    private final Random random;
    private final ForbiddenMoveChecker forbiddenMoveChecker;
    private final GomokuEvaluator gomokuEvaluator;

    public MinimaxBot() {
        this(new HeuristicBot(), new Random());
    }

    public MinimaxBot(HeuristicBot fallback) {
        this(fallback, new Random());
    }

    public MinimaxBot(HeuristicBot fallback, Random random) {
        this.fallback = Objects.requireNonNull(fallback, "fallback cannot be null");
        this.random = Objects.requireNonNull(random, "random cannot be null");
        this.forbiddenMoveChecker = new ForbiddenMoveChecker();
        this.gomokuEvaluator = new GomokuEvaluator();
    }

    @Override
    public BotMoveResult chooseMove(BotMoveRequest request) {
        Objects.requireNonNull(request, "request cannot be null");

        if (isTicTacToe(request)) {
            return chooseTicTacToeMove(request);
        }

        if (isConnectFour(request)) {
            return chooseConnectFourMove(request);
        }

        if (isGomoku(request)) {
            return chooseGomokuMove(request);
        }

        // TODO: Implement Six-in-a-Row search AI; use heuristic fallback for now.
        BotMoveResult result = fallback.chooseMove(request);
        return result.move()
                .map(move -> BotMoveResult.of(move, BotDifficulty.HARD))
                .orElseGet(() -> BotMoveResult.noMove(BotDifficulty.HARD));
    }

    private BotMoveResult chooseTicTacToeMove(BotMoveRequest request) {
        Player[][] board = copyBoard(request.engine());
        List<Move> legalMoves = legalMoves(board, request.botPlayer());
        if (legalMoves.isEmpty()) {
            return BotMoveResult.noMove(BotDifficulty.HARD);
        }

        int bestScore = Integer.MIN_VALUE;
        List<ScoredMove> bestMoves = new ArrayList<>();
        for (Move move : legalMoves) {
            board[move.position().row()][move.position().col()] = request.botPlayer();
            int score = minimax(board, request.botPlayer(), request.opponentPlayer(), request.opponentPlayer(), 1);
            board[move.position().row()][move.position().col()] = null;

            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(new ScoredMove(move, tieBreaker(move)));
            } else if (score == bestScore) {
                bestMoves.add(new ScoredMove(move, tieBreaker(move)));
            }
        }

        int bestTieBreaker = bestMoves.stream()
                .map(ScoredMove::tieBreaker)
                .max(Comparator.naturalOrder())
                .orElse(0);
        List<Move> tiedMoves = bestMoves.stream()
                .filter(move -> move.tieBreaker() == bestTieBreaker)
                .map(ScoredMove::move)
                .toList();
        return BotMoveResult.of(tiedMoves.get(random.nextInt(tiedMoves.size())), BotDifficulty.HARD);
    }

    private boolean isTicTacToe(BotMoveRequest request) {
        return request.gameDefinitionOptional()
                .map(definition -> "ticTacToe".equals(definition.id()))
                .orElse(request.engine().getBoardRows() == TIC_TAC_TOE_SIZE
                        && request.engine().getBoardCols() == TIC_TAC_TOE_SIZE);
    }

    private boolean isConnectFour(BotMoveRequest request) {
        return request.gameDefinitionOptional()
                .map(definition -> "connectFour".equals(definition.id()))
                .orElse(request.engine().getBoardRows() == CONNECT_FOUR_ROWS
                        && request.engine().getBoardCols() == CONNECT_FOUR_COLS);
    }

    private boolean isGomoku(BotMoveRequest request) {
        return request.gameDefinitionOptional()
                .map(definition -> "gomoku".equals(definition.id()))
                .orElse(request.engine() instanceof GomokuEngine);
    }

    private Player[][] copyBoard(GameEngine engine) {
        Player[][] board = new Player[TIC_TAC_TOE_SIZE][TIC_TAC_TOE_SIZE];
        for (int row = 0; row < TIC_TAC_TOE_SIZE; row++) {
            for (int col = 0; col < TIC_TAC_TOE_SIZE; col++) {
                board[row][col] = engine.getStoneAt(new Position(row, col));
            }
        }
        return board;
    }

    private int minimax(Player[][] board, Player botPlayer, Player opponentPlayer, Player currentPlayer, int depth) {
        Player winner = winner(board);
        if (winner == botPlayer) {
            return 10 - depth;
        }
        if (winner == opponentPlayer) {
            return depth - 10;
        }
        if (isFull(board)) {
            return 0;
        }

        boolean maximizing = currentPlayer == botPlayer;
        int bestScore = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (Move move : legalMoves(board, currentPlayer)) {
            board[move.position().row()][move.position().col()] = currentPlayer;
            int score = minimax(board, botPlayer, opponentPlayer, currentPlayer.opposite(), depth + 1);
            board[move.position().row()][move.position().col()] = null;

            if (maximizing) {
                bestScore = Math.max(bestScore, score);
            } else {
                bestScore = Math.min(bestScore, score);
            }
        }
        return bestScore;
    }

    private List<Move> legalMoves(Player[][] board, Player player) {
        List<Move> moves = new ArrayList<>();
        for (int row = 0; row < TIC_TAC_TOE_SIZE; row++) {
            for (int col = 0; col < TIC_TAC_TOE_SIZE; col++) {
                if (board[row][col] == null) {
                    moves.add(new Move(player, new Position(row, col)));
                }
            }
        }
        return moves;
    }

    private Player winner(Player[][] board) {
        for (int i = 0; i < TIC_TAC_TOE_SIZE; i++) {
            Player rowWinner = lineWinner(board[i][0], board[i][1], board[i][2]);
            if (rowWinner != null) {
                return rowWinner;
            }

            Player colWinner = lineWinner(board[0][i], board[1][i], board[2][i]);
            if (colWinner != null) {
                return colWinner;
            }
        }

        Player diagonalWinner = lineWinner(board[0][0], board[1][1], board[2][2]);
        if (diagonalWinner != null) {
            return diagonalWinner;
        }
        return lineWinner(board[0][2], board[1][1], board[2][0]);
    }

    private Player lineWinner(Player a, Player b, Player c) {
        return a != null && a == b && b == c ? a : null;
    }

    private boolean isFull(Player[][] board) {
        for (int row = 0; row < TIC_TAC_TOE_SIZE; row++) {
            for (int col = 0; col < TIC_TAC_TOE_SIZE; col++) {
                if (board[row][col] == null) {
                    return false;
                }
            }
        }
        return true;
    }

    private int tieBreaker(Move move) {
        int row = move.position().row();
        int col = move.position().col();
        if (row == 1 && col == 1) {
            return 2;
        }
        if ((row == 0 || row == 2) && (col == 0 || col == 2)) {
            return 1;
        }
        return 0;
    }

    private BotMoveResult chooseConnectFourMove(BotMoveRequest request) {
        Player[][] board = copyConnectFourBoard(request.engine());
        List<Integer> legalColumns = legalConnectFourColumns(board);
        if (legalColumns.isEmpty()) {
            return BotMoveResult.noMove(BotDifficulty.HARD);
        }

        int bestScore = Integer.MIN_VALUE;
        List<Integer> bestColumns = new ArrayList<>();
        for (int col : legalColumns) {
            int row = dropStone(board, col, request.botPlayer());
            int score = connectFourMinimax(
                    board,
                    request.botPlayer(),
                    request.opponentPlayer(),
                    request.opponentPlayer(),
                    CONNECT_FOUR_SEARCH_DEPTH - 1,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE
            );
            board[row][col] = null;

            if (score > bestScore) {
                bestScore = score;
                bestColumns.clear();
                bestColumns.add(col);
            } else if (score == bestScore) {
                bestColumns.add(col);
            }
        }

        int selectedCol = bestColumns.get(random.nextInt(bestColumns.size()));
        int selectedRow = availableConnectFourRow(board, selectedCol);
        return BotMoveResult.of(
                new Move(request.botPlayer(), new Position(selectedRow, selectedCol)),
                BotDifficulty.HARD
        );
    }

    private Player[][] copyConnectFourBoard(GameEngine engine) {
        Player[][] board = new Player[CONNECT_FOUR_ROWS][CONNECT_FOUR_COLS];
        for (int row = 0; row < CONNECT_FOUR_ROWS; row++) {
            for (int col = 0; col < CONNECT_FOUR_COLS; col++) {
                board[row][col] = engine.getStoneAt(new Position(row, col));
            }
        }
        return board;
    }

    private int connectFourMinimax(Player[][] board, Player botPlayer, Player opponentPlayer,
                                   Player currentPlayer, int depth, int alpha, int beta) {
        Player winner = connectFourWinner(board);
        if (winner == botPlayer) {
            return WIN_SCORE + depth;
        }
        if (winner == opponentPlayer) {
            return -WIN_SCORE - depth;
        }
        if (depth == 0 || legalConnectFourColumns(board).isEmpty()) {
            return evaluateConnectFourBoard(board, botPlayer, opponentPlayer);
        }

        boolean maximizing = currentPlayer == botPlayer;
        if (maximizing) {
            int value = Integer.MIN_VALUE;
            for (int col : legalConnectFourColumns(board)) {
                int row = dropStone(board, col, currentPlayer);
                value = Math.max(value, connectFourMinimax(
                        board,
                        botPlayer,
                        opponentPlayer,
                        opponentPlayer,
                        depth - 1,
                        alpha,
                        beta
                ));
                board[row][col] = null;
                alpha = Math.max(alpha, value);
                if (alpha >= beta) {
                    break;
                }
            }
            return value;
        }

        int value = Integer.MAX_VALUE;
        for (int col : legalConnectFourColumns(board)) {
            int row = dropStone(board, col, currentPlayer);
            value = Math.min(value, connectFourMinimax(
                    board,
                    botPlayer,
                    opponentPlayer,
                    botPlayer,
                    depth - 1,
                    alpha,
                    beta
            ));
            board[row][col] = null;
            beta = Math.min(beta, value);
            if (alpha >= beta) {
                break;
            }
        }
        return value;
    }

    private List<Integer> legalConnectFourColumns(Player[][] board) {
        List<Integer> columns = new ArrayList<>();
        for (int col : orderedConnectFourColumns()) {
            if (board[0][col] == null) {
                columns.add(col);
            }
        }
        return columns;
    }

    private List<Integer> orderedConnectFourColumns() {
        return List.of(3, 2, 4, 1, 5, 0, 6);
    }

    private int availableConnectFourRow(Player[][] board, int col) {
        for (int row = CONNECT_FOUR_ROWS - 1; row >= 0; row--) {
            if (board[row][col] == null) {
                return row;
            }
        }
        return -1;
    }

    private int dropStone(Player[][] board, int col, Player player) {
        int row = availableConnectFourRow(board, col);
        if (row < 0) {
            throw new IllegalStateException("Column is full: " + col);
        }
        board[row][col] = player;
        return row;
    }

    private Player connectFourWinner(Player[][] board) {
        for (int row = 0; row < CONNECT_FOUR_ROWS; row++) {
            for (int col = 0; col < CONNECT_FOUR_COLS; col++) {
                Player player = board[row][col];
                if (player == null) {
                    continue;
                }
                for (int[] direction : CONNECT_FOUR_DIRECTIONS) {
                    if (hasConnectFourLine(board, row, col, direction[0], direction[1], player)) {
                        return player;
                    }
                }
            }
        }
        return null;
    }

    private boolean hasConnectFourLine(Player[][] board, int row, int col, int dr, int dc, Player player) {
        for (int step = 1; step < CONNECT_FOUR_WIN_COUNT; step++) {
            int nextRow = row + dr * step;
            int nextCol = col + dc * step;
            if (!isConnectFourInside(nextRow, nextCol) || board[nextRow][nextCol] != player) {
                return false;
            }
        }
        return true;
    }

    private int evaluateConnectFourBoard(Player[][] board, Player botPlayer, Player opponentPlayer) {
        int score = 0;
        int centerCol = CONNECT_FOUR_COLS / 2;
        for (int row = 0; row < CONNECT_FOUR_ROWS; row++) {
            if (board[row][centerCol] == botPlayer) {
                score += 45;
            } else if (board[row][centerCol] == opponentPlayer) {
                score -= 45;
            }
        }

        for (int row = 0; row < CONNECT_FOUR_ROWS; row++) {
            for (int col = 0; col < CONNECT_FOUR_COLS; col++) {
                for (int[] direction : CONNECT_FOUR_DIRECTIONS) {
                    if (isConnectFourInside(
                            row + direction[0] * (CONNECT_FOUR_WIN_COUNT - 1),
                            col + direction[1] * (CONNECT_FOUR_WIN_COUNT - 1)
                    )) {
                        score += evaluateConnectFourWindow(board, row, col, direction[0], direction[1],
                                botPlayer, opponentPlayer);
                    }
                }
            }
        }
        return score;
    }

    private int evaluateConnectFourWindow(Player[][] board, int row, int col, int dr, int dc,
                                          Player botPlayer, Player opponentPlayer) {
        int botCount = 0;
        int opponentCount = 0;
        int emptyCount = 0;
        for (int step = 0; step < CONNECT_FOUR_WIN_COUNT; step++) {
            Player stone = board[row + dr * step][col + dc * step];
            if (stone == botPlayer) {
                botCount++;
            } else if (stone == opponentPlayer) {
                opponentCount++;
            } else {
                emptyCount++;
            }
        }

        if (botCount > 0 && opponentCount > 0) {
            return 0;
        }
        if (botCount == 4) {
            return 100_000;
        }
        if (opponentCount == 4) {
            return -100_000;
        }
        if (botCount == 3 && emptyCount == 1) {
            return 6_000;
        }
        if (opponentCount == 3 && emptyCount == 1) {
            return -8_000;
        }
        if (botCount == 2 && emptyCount == 2) {
            return 600;
        }
        if (opponentCount == 2 && emptyCount == 2) {
            return -700;
        }
        if (botCount == 1 && emptyCount == 3) {
            return 40;
        }
        if (opponentCount == 1 && emptyCount == 3) {
            return -45;
        }
        return 0;
    }

    private boolean isConnectFourInside(int row, int col) {
        return row >= 0 && row < CONNECT_FOUR_ROWS && col >= 0 && col < CONNECT_FOUR_COLS;
    }

    private BotMoveResult chooseGomokuMove(BotMoveRequest request) {
        Player[][] board = copyVariableBoard(request.engine());
        boolean forbiddenMovesEnabled = isForbiddenMovesEnabled(request.engine());
        List<Move> candidates = gomokuCandidates(
                board,
                request.botPlayer(),
                request.opponentPlayer(),
                request.botPlayer(),
                forbiddenMovesEnabled,
                GOMOKU_TOP_CANDIDATES
        );
        if (candidates.isEmpty()) {
            return BotMoveResult.noMove(BotDifficulty.HARD);
        }

        Optional<Move> immediateWin = firstLineCompletingMove(board, candidates, request.botPlayer());
        if (immediateWin.isPresent()) {
            return BotMoveResult.of(immediateWin.get(), BotDifficulty.HARD);
        }

        Optional<Move> immediateBlock = firstLineCompletingMove(board, candidates, request.opponentPlayer());
        if (immediateBlock.isPresent()) {
            return BotMoveResult.of(new Move(request.botPlayer(), immediateBlock.get().position()), BotDifficulty.HARD);
        }

        int bestScore = Integer.MIN_VALUE;
        List<Move> bestMoves = new ArrayList<>();
        for (Move move : candidates) {
            place(board, move);
            int score = completesLine(board, move.position(), request.botPlayer(), GOMOKU_TARGET_LENGTH)
                    ? WIN_SCORE
                    : gomokuAlphaBeta(
                    board,
                    request.botPlayer(),
                    request.opponentPlayer(),
                    request.opponentPlayer(),
                    GOMOKU_SEARCH_DEPTH - 1,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE,
                    forbiddenMovesEnabled
            );
            remove(board, move);

            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (score == bestScore) {
                bestMoves.add(move);
            }
        }

        return BotMoveResult.of(bestMoves.get(random.nextInt(bestMoves.size())), BotDifficulty.HARD);
    }

    private int gomokuAlphaBeta(Player[][] board, Player botPlayer, Player opponentPlayer, Player currentPlayer,
                                int depth, int alpha, int beta, boolean forbiddenMovesEnabled) {
        if (depth == 0 || isBoardFull(board)) {
            return evaluateGomokuPosition(board, botPlayer, opponentPlayer);
        }

        List<Move> candidates = gomokuCandidates(
                board,
                botPlayer,
                opponentPlayer,
                currentPlayer,
                forbiddenMovesEnabled,
                GOMOKU_TOP_CANDIDATES
        );
        if (candidates.isEmpty()) {
            return evaluateGomokuPosition(board, botPlayer, opponentPlayer);
        }

        boolean maximizing = currentPlayer == botPlayer;
        if (maximizing) {
            int value = Integer.MIN_VALUE;
            for (Move move : candidates) {
                place(board, move);
                int score = completesLine(board, move.position(), currentPlayer, GOMOKU_TARGET_LENGTH)
                        ? WIN_SCORE + depth
                        : gomokuAlphaBeta(board, botPlayer, opponentPlayer, opponentPlayer,
                        depth - 1, alpha, beta, forbiddenMovesEnabled);
                remove(board, move);
                value = Math.max(value, score);
                alpha = Math.max(alpha, value);
                if (alpha >= beta) {
                    break;
                }
            }
            return value;
        }

        int value = Integer.MAX_VALUE;
        for (Move move : candidates) {
            place(board, move);
            int score = completesLine(board, move.position(), currentPlayer, GOMOKU_TARGET_LENGTH)
                    ? -WIN_SCORE - depth
                    : gomokuAlphaBeta(board, botPlayer, opponentPlayer, botPlayer,
                    depth - 1, alpha, beta, forbiddenMovesEnabled);
            remove(board, move);
            value = Math.min(value, score);
            beta = Math.min(beta, value);
            if (alpha >= beta) {
                break;
            }
        }
        return value;
    }

    private List<Move> gomokuCandidates(Player[][] board, Player botPlayer, Player opponentPlayer,
                                        Player currentPlayer, boolean forbiddenMovesEnabled, int limit) {
        List<Move> candidates = new ArrayList<>();
        int rows = board.length;
        int cols = board[0].length;
        if (!hasAnyStone(board)) {
            candidates.add(new Move(currentPlayer, new Position(rows / 2, cols / 2)));
            return candidates;
        }

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (board[row][col] != null || !isNearExistingStone(board, row, col, 2)) {
                    continue;
                }
                Move move = new Move(currentPlayer, new Position(row, col));
                if (isAllowedGomokuSearchMove(board, move, forbiddenMovesEnabled)) {
                    candidates.add(move);
                }
            }
        }

        List<ScoredMove> scoredMoves = candidates.stream()
                .map(move -> new ScoredMove(move, quickGomokuSearchScore(board, move, botPlayer, opponentPlayer)))
                .sorted(Comparator.comparingInt(ScoredMove::tieBreaker).reversed())
                .toList();

        if (scoredMoves.size() > limit) {
            List<Move> prunedMoves = new ArrayList<>();
            for (ScoredMove scoredMove : scoredMoves) {
                if (scoredMove.tieBreaker() >= GOMOKU_TACTICAL_KEEP_SCORE) {
                    prunedMoves.add(scoredMove.move());
                }
            }
            for (ScoredMove scoredMove : scoredMoves) {
                if (prunedMoves.size() >= limit && scoredMove.tieBreaker() < GOMOKU_TACTICAL_KEEP_SCORE) {
                    break;
                }
                if (!prunedMoves.contains(scoredMove.move())) {
                    prunedMoves.add(scoredMove.move());
                }
            }
            return prunedMoves;
        }
        return scoredMoves.stream().map(ScoredMove::move).toList();
    }

    private int quickGomokuSearchScore(Player[][] board, Move move, Player botPlayer, Player opponentPlayer) {
        BotMoveRequest request = new BotMoveRequest(
                new ArrayBoardEngine(board),
                move.player(),
                move.player() == botPlayer ? opponentPlayer : botPlayer,
                null
        );
        return gomokuEvaluator.evaluateMove(request, move);
    }

    private Optional<Move> firstLineCompletingMove(Player[][] board, List<Move> candidates, Player player) {
        for (Move move : candidates) {
            Move candidate = new Move(player, move.position());
            if (completesLine(board, candidate.position(), player, GOMOKU_TARGET_LENGTH)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    private int evaluateGomokuPosition(Player[][] board, Player botPlayer, Player opponentPlayer) {
        ArrayBoardEngine engine = new ArrayBoardEngine(board);
        int botScore = bestGomokuLeafScore(engine, board, botPlayer, opponentPlayer);
        int opponentScore = bestGomokuLeafScore(engine, board, opponentPlayer, botPlayer);
        return botScore - opponentScore;
    }

    private int bestGomokuLeafScore(ArrayBoardEngine engine, Player[][] board, Player player, Player opponent) {
        int best = 0;
        for (Move move : gomokuCandidates(board, player, opponent, player, false, GOMOKU_TOP_CANDIDATES)) {
            BotMoveRequest request = new BotMoveRequest(engine, player, opponent, null);
            best = Math.max(best, gomokuEvaluator.evaluateMove(request, move));
        }
        return best;
    }

    private Player[][] copyVariableBoard(GameEngine engine) {
        Player[][] board = new Player[engine.getBoardRows()][engine.getBoardCols()];
        for (int row = 0; row < engine.getBoardRows(); row++) {
            for (int col = 0; col < engine.getBoardCols(); col++) {
                board[row][col] = engine.getStoneAt(new Position(row, col));
            }
        }
        return board;
    }

    private boolean isForbiddenMovesEnabled(GameEngine engine) {
        return engine instanceof GomokuEngine gomokuEngine && gomokuEngine.isForbiddenMovesEnabled();
    }

    private boolean isAllowedGomokuSearchMove(Player[][] board, Move move, boolean forbiddenMovesEnabled) {
        if (!forbiddenMovesEnabled || move.player() != Player.BLACK) {
            return true;
        }

        Board boardCopy = new Board(board.length);
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] != null) {
                    boardCopy.placeStone(board[row][col], new Position(row, col));
                }
            }
        }
        boardCopy.placeStone(Player.BLACK, move.position());
        return !forbiddenMoveChecker.isForbidden(boardCopy, move.position());
    }

    private void place(Player[][] board, Move move) {
        board[move.position().row()][move.position().col()] = move.player();
    }

    private void remove(Player[][] board, Move move) {
        board[move.position().row()][move.position().col()] = null;
    }

    private boolean hasAnyStone(Player[][] board) {
        for (Player[] row : board) {
            for (Player stone : row) {
                if (stone != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isBoardFull(Player[][] board) {
        for (Player[] row : board) {
            for (Player stone : row) {
                if (stone == null) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isNearExistingStone(Player[][] board, int moveRow, int moveCol, int radius) {
        for (int row = moveRow - radius; row <= moveRow + radius; row++) {
            for (int col = moveCol - radius; col <= moveCol + radius; col++) {
                if ((row == moveRow && col == moveCol)
                        || row < 0 || row >= board.length
                        || col < 0 || col >= board[0].length) {
                    continue;
                }
                if (board[row][col] != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean completesLine(Player[][] board, Position position, Player player, int targetLength) {
        int[][] directions = {
                {1, 0},
                {0, 1},
                {1, 1},
                {1, -1}
        };
        for (int[] direction : directions) {
            int count = 1
                    + countDirection(board, position, player, direction[0], direction[1])
                    + countDirection(board, position, player, -direction[0], -direction[1]);
            if (count >= targetLength) {
                return true;
            }
        }
        return false;
    }

    private int countDirection(Player[][] board, Position position, Player player, int dr, int dc) {
        int count = 0;
        int row = position.row() + dr;
        int col = position.col() + dc;
        while (row >= 0 && row < board.length
                && col >= 0 && col < board[0].length
                && board[row][col] == player) {
            count++;
            row += dr;
            col += dc;
        }
        return count;
    }

    private record ScoredMove(Move move, int tieBreaker) {
    }

    private static class ArrayBoardEngine implements GameEngine {
        private final Player[][] board;

        private ArrayBoardEngine(Player[][] board) {
            this.board = board;
        }

        @Override
        public int getBoardSize() {
            return board.length;
        }

        @Override
        public int getBoardRows() {
            return board.length;
        }

        @Override
        public int getBoardCols() {
            return board[0].length;
        }

        @Override
        public Player getCurrentPlayer() {
            return Player.BLACK;
        }

        @Override
        public GameResult getResult() {
            return GameResult.inProgress();
        }

        @Override
        public GameStatus getStatus() {
            return GameStatus.IN_PROGRESS;
        }

        @Override
        public String getLatestMessage() {
            return "";
        }

        @Override
        public Optional<Move> getLastMove() {
            return Optional.empty();
        }

        @Override
        public Player getStoneAt(Position position) {
            return board[position.row()][position.col()];
        }

        @Override
        public boolean isGameOver() {
            return false;
        }

        @Override
        public boolean makeMove(Position position) {
            throw new UnsupportedOperationException("Search board is read-only.");
        }

        @Override
        public boolean undo() {
            throw new UnsupportedOperationException("Search board is read-only.");
        }

        @Override
        public void reset() {
            throw new UnsupportedOperationException("Search board is read-only.");
        }

        @Override
        public void forceTimeoutLoss(Player loser) {
            throw new UnsupportedOperationException("Search board is read-only.");
        }
    }
}
