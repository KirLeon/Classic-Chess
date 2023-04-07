package com.mrk.bsuir.logic;

import com.mrk.bsuir.log.GameLogService;
import com.mrk.bsuir.model.Board;
import com.mrk.bsuir.model.Color;
import com.mrk.bsuir.model.Piece;
import com.mrk.bsuir.model.impl.Bishop;
import com.mrk.bsuir.model.impl.King;
import com.mrk.bsuir.model.impl.Knight;
import com.mrk.bsuir.model.impl.Pawn;
import com.mrk.bsuir.model.impl.Queen;
import com.mrk.bsuir.model.impl.Rook;

public class MoveService {

    private Board board;
    private GameLogService logService;

    public MoveService(Board board, GameLogService logService) {
        this.board = board;
        this.logService = logService;
    }

    //Defining valid coordinates for any piece
    private final int minAllowableX = 0;
    private final int maxAllowableX = 7;
    private int minAllowableY;
    private int maxAllowableY;

    private boolean isBeating;
    private boolean isFirstMove;

    public boolean allowPieceMove(int startX, int startY, int endX, int endY, Piece piece) {

        //checking coordinates
        if (startX < minAllowableX || endX > maxAllowableX || startY < 0 || endY > 7) {
            return false;
        }

        //checking color
        if (!piece.getColor().equals(Color.WHITE) && !piece.getColor().equals(Color.BLACK)) {
            return false;
        }

        if (piece.isProtectingKing()) {
            return false;
        }

        boolean validMove = false;

        if (piece instanceof Pawn) {
            validMove = allowPawnStraightMove(startX, startY, endX, endY, (Pawn) piece);
        } else if (piece instanceof Bishop) {
            validMove = allowDiagonalMove(startX, startY, endX, endY);
        } else if (piece instanceof Knight) {
            validMove = allowKnightJump(startX, startY, endX, endY);
        } else if (piece instanceof Rook) {
            validMove = allowStraightMove(startX, startY, endX, endY, (Rook) piece);
        } else if (piece instanceof Queen) {
            validMove = allowStraightMove(startX, startY, endX, endY, piece) ||
                    allowDiagonalMove(startX, startY, endX, endY);
        } else if (piece instanceof King) {
            validMove = allowKingAroundMove(startX, startY, endX, endY, (King) piece);
        } else throw new RuntimeException("Incorrect piece");

        return validMove;
    }

    public boolean allowPawnAttackMove(int startY, int endX, int endY, Pawn pawn) {

        if (Math.abs(startY - endY) != 1) return false;

        Piece pieceOnAttackedCell = board.getPieceFromCell(endX, endY);

        if (pieceOnAttackedCell != null) {
            pawn.setFirstMove(false);
            return true;
        } else {

            String regEx = "^P[0-7][0-7][0-7][0-7]$";
            String lastMove = logService.getLastMove();

            if (lastMove.matches(regEx)) {

                int lastMoveStartX = lastMove.charAt(1);
                int lastMoveStartY = lastMove.charAt(2);
                int lastMoveEndX = lastMove.charAt(3);
                int lastMoveEndY = lastMove.charAt(4);

                //in case this move was from white, the middle cord is StartY + 1, else vice versa
                int middleCell = lastMoveEndY > lastMoveStartY ? lastMoveStartY + 1
                        : lastMoveStartY - 1;

                //checking firstMove and correct capturing of enemy pawn
                if (Math.abs(lastMoveEndY - lastMoveStartY) == 2 && endY == middleCell &&
                        lastMoveStartX == lastMoveEndX && lastMoveEndX == endX) {

                    pawn.setFirstMove(false);
                    return true;
                }

            }
            return false;
        }
    }

    public boolean allowPawnStraightMove(int startX, int startY, int endX, int endY, Pawn pawn) {

        if (pawn.isProtectingKing()) return false;

        Color color = pawn.getColor();
        boolean isFirstMove = pawn.isFirstMove();

        //min & max Y coordinate are different for white and black pawns
        if (color.equals(Color.WHITE)) {
            minAllowableY = 1;
            maxAllowableY = 7;

            if (startY > endY) {
                return false;
            }

        } else {
            minAllowableY = 6;
            maxAllowableY = 0;

            if (startY < endY) {
                return false;
            }
        }


        //if coordinates are incorrect
        if (startY < minAllowableY || endY > maxAllowableY) {
            return false;
        }

        //Cannot move pawn further than 1 cell horizontal
        if (Math.abs(startX - endX) > 1) return false;

            //Case horizontal cell changed check the ability to attack
        else if (Math.abs(startX - endX) == 1)
            return allowPawnAttackMove(startY, endX, endY, pawn);

        //if something blocking the way
        if (board.getPieceFromCell(endX, endY) != null) return false;

        //Pawn can move 2 cells forward only in case it's moving first time
        if (!isFirstMove && Math.abs(startY - endY) > 1) {
            return false;
        }

        pawn.setFirstMove(false);
        return true;
    }

    public boolean allowDiagonalMove(int startX, int startY, int endX, int endY) {

        //color of the cell should be the same and delta of (x,y) coordinates should be same too
        if (!board.getCellColor(startX, startY).equals(board.getCellColor(endX, endY)) ||
                Math.abs(startX - endX) != Math.abs(startY - endY)) {
            return false;
        }

        int x = startX;
        int y = startY;
        int xStep = Integer.compare(endX, startX);
        int yStep = Integer.compare(endY, startY);

        //checking obstacles in the diagonal of movement
        while (x != endX || y != endY) {
            x += xStep;
            y += yStep;
            if (board.getPieceFromCell(x, y) != null) {
                return false;
            }
        }

        return true;
    }

    public boolean allowStraightMove(int startX, int startY, int endX, int endY, Piece piece) {

        //only one of lines (horizontal or vertical) is allowed to change
        if (Math.abs(startX - endX) != 0 && Math.abs(startY - endY) != 0) {
            return false;
        }

        //checking obstacles in the line of movement
        if (startX == endX) {
            for (int i = Math.max(startY, endY); i > Math.min(startY, endY); i--) {
                if (board.getPieceFromCell(endX, i) != null) return false;
            }
        } else {
            for (int i = Math.max(startX, endX); i > Math.min(startX, endX); i--) {
                if (board.getPieceFromCell(i, endY) != null) return false;
            }
        }

        //for canceling the ability of castling after rook's first move
        if (piece instanceof Rook) {
            ((Rook) piece).setFirstMove(false);
        }
        return true;
    }

    public boolean allowKingAroundMove(int startX, int startY, int endX, int endY, King king) {

        if (Math.abs(startX - endX) == 2) return allowCastling(endX, endY, king);
        else if (Math.abs(startX - endX) > 1 || Math.abs(startY - endY) > 1
                || board.getPieceFromCell(endX, endY) != null
                || board.isCellUnderAttackForColor(endX, endY, king.getColor())) {
            return false;
        }

        king.setFirstMove(false);
        return true;

    }

    public boolean allowCastling(int endX, int endY, King king) {

        //cannot castle while king is under check or it is not his first move
        if (!king.isFirstMove() || king.isUnderCheck()) return false;

        int firstStepCell = 4 - Integer.compare(4, endX);
        int secondStepCell = 4 - (Integer.compare(4, endX) * 2);
        int rookCellX = 4 > endX ? 0 : 7;

        //if the rook haven't moved yet (check second condition) and its color same as the king's ->
        //the rook is definitely situated at king's line (we are sure that king haven't move yet)
        Piece piece = board.getPieceFromCell(rookCellX, endY);
        if (!(piece instanceof Rook) || !((Rook) piece).isFirstMove()
                || !piece.getColor().equals(king.getColor())) {
            return false;
        }


        if (board.getPieceFromCell(firstStepCell, endY) != null
                || board.getPieceFromCell(secondStepCell, endY) != null
                || board.isCellUnderAttackForColor(firstStepCell, endY, king.getColor())
                || board.isCellUnderAttackForColor(secondStepCell, endY, king.getColor())) {
            return false;
        }

        return true;
    }

    private boolean allowKnightJump(int startX, int startY, int endX, int endY) {

        int deltaX = Math.abs(startX - endX);
        int deltaY = Math.abs(startY - endY);

        if (deltaX + deltaY != 3 || deltaX < 1 || deltaY < 1) {
            return false;
        }

        return true;
    }

    private void clearMoveServiceParameters() {
        minAllowableY = 0;
        maxAllowableY = 0;
        isBeating = false;
        isFirstMove = false;
    }


}
