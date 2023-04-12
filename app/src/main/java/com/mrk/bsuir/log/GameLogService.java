package com.mrk.bsuir.log;

import com.mrk.bsuir.exceptions.LoggingException;
import com.mrk.bsuir.logic.Action;
import com.mrk.bsuir.model.Board;
import com.mrk.bsuir.model.Color;
import com.mrk.bsuir.model.Piece;
import com.mrk.bsuir.model.impl.Bishop;
import com.mrk.bsuir.model.impl.King;
import com.mrk.bsuir.model.impl.Knight;
import com.mrk.bsuir.model.impl.Pawn;
import com.mrk.bsuir.model.impl.Queen;
import com.mrk.bsuir.model.impl.Rook;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameLogService {

    private final Map<Integer, String> gameMovesRecord;
    private final Map<Class<? extends Piece>, String> piecesSymbols;
    private final Map<Integer, Piece> beatenPieces;
    private int currentMove;
    private final Board board;


    public GameLogService(Board board) {
        this.board = board;
        currentMove = 0;
        piecesSymbols = new HashMap<>();
        gameMovesRecord = new HashMap<>();
        beatenPieces = new HashMap<>();

        piecesSymbols.put(Pawn.class, "P");
        piecesSymbols.put(Knight.class, "N");
        piecesSymbols.put(Bishop.class, "B");
        piecesSymbols.put(Rook.class, "R");
        piecesSymbols.put(Queen.class, "Q");
        piecesSymbols.put(King.class, "K");
    }

    public void logMove(int startX, int startY, int endX, int endY, Piece movingPiece,
                        Piece capturedPiece, Piece promotedPiece, List<Action> actions) {

        if (actions == null || (actions.contains(Action.PROMOTION) && promotedPiece == null) ||
                movingPiece == null ||
                (actions.contains(Action.CAPTURE) && capturedPiece == null)) {
            throw new LoggingException("Error: cannot log");
        }

        StringBuilder moveBuilder = new StringBuilder();

        if (actions.contains(Action.LONG_CASTLING)) {
            moveBuilder.append(Action.LONG_CASTLING.getLoggingSymbol());
        } else if (actions.contains(Action.SHORT_CASTLING)) {
            moveBuilder.append(Action.SHORT_CASTLING.getLoggingSymbol());
        } else {

            moveBuilder.append(piecesSymbols.get(movingPiece.getClass()));
            moveBuilder.append(startX).append(startY);

            if (actions.contains(Action.CAPTURE)) {

                moveBuilder.append(Action.CAPTURE.getLoggingSymbol());
                moveBuilder.append(piecesSymbols.get(capturedPiece.getClass()));
                beatenPieces.put(currentMove, capturedPiece);
            } else if (actions.contains(Action.EN_PASSANT)) {

                moveBuilder.append(Action.EN_PASSANT.getLoggingSymbol());
                beatenPieces.put(currentMove, capturedPiece);
            }
            moveBuilder.append(endX).append(endY);

            if (actions.contains(Action.PROMOTION)) {

                moveBuilder.append(Action.PROMOTION.getLoggingSymbol());
                moveBuilder.append(piecesSymbols.get(promotedPiece.getClass()));
            }
            if (actions.contains(Action.CHECKMATE)) {
                moveBuilder.append(Action.CHECKMATE.getLoggingSymbol());
            } else if (actions.contains(Action.CHECK)) {
                moveBuilder.append(Action.CHECK.getLoggingSymbol());
            }
        }

        gameMovesRecord.put(currentMove, moveBuilder.toString());
        currentMove++;
    }

    public Map<Piece, Integer[]> getPreviousPosition() {

        String lastMove = getMoveByNumber(currentMove - 1);
        Map<Piece, Integer[]> previousPosition = new HashMap<>();

        Color previousColor = (currentMove - 1) % 2 == 0 ? Color.WHITE : Color.BLACK;
        Color currentColor = (currentMove - 1) % 2 == 0 ? Color.BLACK : Color.WHITE;

        King king = board.getKingOfThisColor(previousColor);
        King enemyKing = board.getKingOfAnotherColor(previousColor);

        if (lastMove.endsWith("+")) {
            enemyKing.setUnderCheck(false);
        }

        if (lastMove.startsWith("0-")) {

            int rookY = previousColor.equals(Color.WHITE) ? 0 : 7;
            int rookX = lastMove.length() > 4 ? 0 : 7;

            Rook rook = new Rook(previousColor, rookX, rookY);

            previousPosition.put(king, new Integer[]{4, rookY});
            previousPosition.put(rook, new Integer[]{0, rookY});
            currentMove--;
            return previousPosition;
        }

        int startX = Character.digit(lastMove.charAt(1), 10);
        int startY = Character.digit(lastMove.charAt(2), 10);
        int pawnMovementDirection = currentColor.equals(Color.WHITE) ? 1 : -1;

        if (lastMove.contains("e.p.")) {

            int endX = Character.digit(lastMove.charAt(7), 10);
            int endY = Character.digit(lastMove.charAt(8), 10);

            Pawn attackerPawn = (Pawn) board.getPieceFromCell(endX, endY);
            Pawn beatenPawn = (Pawn) findBeatenPiece(currentMove - 1);

            previousPosition.put(beatenPawn, new Integer[]{endX, endY + pawnMovementDirection});
            previousPosition.put(attackerPawn, new Integer[]{startX, startY});
            currentMove--;
            return previousPosition;
        }

        if (lastMove.contains("x")) {
            int endX = Character.digit(lastMove.charAt(5), 10);
            int endY = Character.digit(lastMove.charAt(6), 10);

            Piece beatenPiece = findBeatenPiece(currentMove - 1);
            Piece attackerPiece = board.getPieceFromCell(endX, endY);

            previousPosition.put(attackerPiece, new Integer[]{startX, startY});
            previousPosition.put(beatenPiece, new Integer[]{endX, endY});
        }

        int endX = Character.digit(lastMove.charAt(3), 10);
        int endY = Character.digit(lastMove.charAt(4), 10);

        if (lastMove.contains("=") && previousPosition.isEmpty()) {

            Pawn pawn = (Pawn) beatenPieces.get(currentMove - 1);
            previousPosition.put(pawn, new Integer[]{startX, startY});
            previousPosition.put(null, new Integer[]{endX, endY});
        }

        if (previousPosition.isEmpty()) {
            Piece lastMovedPiece = board.getPieceFromCell(endX, endY);
            previousPosition.put(lastMovedPiece, new Integer[]{startX, startY});
            previousPosition.put(null, new Integer[]{endX, endY});
        }

        currentMove--;
        return previousPosition;
    }

    public Piece findBeatenPiece(int deathMove) {

        Piece beatenPiece = beatenPieces.get(deathMove);
        if (beatenPiece == null) {
            throw new LoggingException("Cannot find beaten piece");
        }

        return beatenPiece;
    }

    public String getMoveByNumber(int moveNumber) {
        return gameMovesRecord.get(moveNumber);
    }


    public String getLastMove() {
        if (currentMove == 1) return null;
        return gameMovesRecord.get(currentMove - 2);
    }

    public int getCurrentMove() {
        return currentMove;
    }

}
