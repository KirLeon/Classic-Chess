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

import java.util.ArrayList;
import java.util.List;

public class MoveService {

    private Board board;
    private GameLogService logService;
    final String REGEX_MOVE_PATTERN = "^P[0-7][0-7][0-7][0-7]$";

    public MoveService(Board board, GameLogService logService) {
        this.board = board;
        this.logService = logService;
    }

    //Defining valid coordinates for any piece
    private final int minAllowableX = 0;
    private final int maxAllowableX = 7;

    public boolean allowPieceMove(int startX, int startY, int endX, int endY, Piece piece) {

        //checking coordinates
        if (startX < minAllowableX || endX > maxAllowableX || startY < 0 || endY > 7) {
            return false;
        }

        if (startX == endX && startY == endY) {
            return false;
        }

        // Check is piece protecting the king
        King king = piece.getColor().equals(Color.WHITE) ? board.getWhiteKing() :
                board.getBlackKing();
        int kingX = board.getPieceCords(king)[0];
        int kingY = board.getPieceCords(king)[1];

        if (checkIfProtectingKing(startX, startY, piece, kingX, kingY)) {
            return false;
        }

        boolean validMove;

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

            String lastMove = logService.getLastMove();

            if (lastMove.matches(REGEX_MOVE_PATTERN)) {

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

        Color color = pawn.getColor();
        boolean isFirstMove = pawn.isFirstMove();

        //Checking for correct direction of pawn movement, allowed amount of crossed cells
        if (Math.abs(startX - endX) > 1) return false;

        int direction = color == Color.WHITE ? 1 : -1;
        boolean longMove = isFirstMove && endY - startY == 2 * direction &&
                board.getPieceFromCell(startX, startY + direction) == null;

        if (endY - startY != direction && !(longMove)) {
            return false;
        }

        //Case horizontal cell changed check the ability to attack
        else if (Math.abs(startX - endX) == 1)
            return allowPawnAttackMove(startY, endX, endY, pawn);

        //if something blocking the way
        if (board.getPieceFromCell(endX, endY) != null) return false;

        //Pawn can move 2 cells forward only in case it's moving first time


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
        int xStep = endX - startX > 0 ? 1 : -1;
        int yStep = endY - startY > 0 ? 1 : -1;
        int iterations = Math.abs(startX - endX);

        //checking obstacles in the diagonal of movement
        for (int i = 1; i < iterations; i++) {
            if (board.getPieceFromCell(startX + i * xStep, startY + i * yStep) != null) {
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
            for (int i = Math.max(startY, endY) - 1; i > Math.min(startY, endY); i--) {
                if (board.getPieceFromCell(endX, i) != null) return false;
            }
        } else {
            for (int i = Math.max(startX, endX) - 1; i > Math.min(startX, endX); i--) {
                if (board.getPieceFromCell(i, endY) != null) return false;
            }
        }

        //for canceling the ability of castling after rook's first move
        if (piece instanceof Rook) {
            ((Rook) piece).setFirstMove(false);
        }

        System.out.println("WWWWWWW Piece can move straight");
        return true;
    }

    public boolean allowKingAroundMove(int startX, int startY, int endX, int endY, King king) {

        int xDiff = Math.abs(startX - endX);
        int yDiff = Math.abs(startY - endY);

        // Finding enemy king to check for its attack on this cell
        King enemyKing = king.getColor().equals(Color.WHITE)
                ? board.getBlackKing()
                : board.getWhiteKing();
        int enemyKingX = board.getPieceCords(enemyKing)[0];
        int enemyKingY = board.getPieceCords(enemyKing)[1];

        // If the move is a castling move, check if it is allowed
        if (xDiff == 2)
            return allowCastling(endX, endY, king, enemyKingX, enemyKingY);
            // Otherwise, check if the move is valid

        else if (xDiff > 1 || yDiff > 1
                || board.getPieceFromCell(endX, endY) != null
                || canBePieceOfThisColorAttackedOnCell(endX, endY, king.getColor())
                || isThisCellAttackedByThisKing(enemyKingX, enemyKingY, endX, endY)) {
            return false;
        }

        king.setFirstMove(false);
        return true;

    }

    // Checking only ability to attack, not to move to avoid the recursive calls (Board 100 line)
    public boolean isThisCellAttackedByThisKing(int kingX, int kingY, int x, int y) {
        return Math.abs(x - kingX) < 2 && Math.abs(y - kingY) < 2;
    }

    public boolean allowCastling(int endX, int endY, King king, int enemyKingX, int enemyKingY) {

        // Cannot castle while king is under check or it is not his first move
        if (!king.isFirstMove() || king.isUnderCheck()) return false;

        int firstStepCellX = 4 - Integer.compare(4, endX);
        int secondStepCellY = 4 - (Integer.compare(4, endX) * 2);
        int rookCellX = 4 > endX ? 0 : 7;

        //if the rook haven't moved yet (check second condition) and its color same as the king's ->
        //the rook is definitely situated at king's line (we are sure that king haven't move yet)
        Piece piece = board.getPieceFromCell(rookCellX, endY);
        if (!(piece instanceof Rook) || !((Rook) piece).isFirstMove()
                || !piece.getColor().equals(king.getColor())) {
            return false;
        }

        //checking attack on cells between king's start and end x coordinate
        if (board.getPieceFromCell(firstStepCellX, endY) != null
                || board.getPieceFromCell(secondStepCellY, endY) != null
                || canBePieceOfThisColorAttackedOnCell(firstStepCellX, endY, king.getColor())
                || isThisCellAttackedByThisKing(enemyKingX, enemyKingY, firstStepCellX, endY)
                || canBePieceOfThisColorAttackedOnCell(secondStepCellY, endY, king.getColor())
                || isThisCellAttackedByThisKing(enemyKingX, enemyKingY, secondStepCellY, endY)) {
            return false;
        }

        return true;
    }

    private boolean allowKnightJump(int startX, int startY, int endX, int endY) {

        int xDiff = Math.abs(startX - endX);
        int yDiff = Math.abs(startY - endY);

        if (xDiff + yDiff != 3 || xDiff < 1 || yDiff < 1) {
            return false;
        }

        return true;
    }


    public boolean canBePieceOfThisColorAttackedOnCell(int endX, int endY, Color kingColor) {

        Color enemyColor = kingColor.equals(Color.WHITE) ? Color.BLACK : Color.WHITE;
        Piece targetPiece;

        for (int startX = 0; startX < 8; startX++) {
            for (int startY = 0; startY < 8; startY++) {

                targetPiece = board.getPieceFromCell(startX, startY);

                //excluded king from that method to avoid recursive calls and split king's moves
                if (targetPiece == null || targetPiece instanceof King) {
                    continue;
                }

                //pawn straight move is available as a move, but not as attack move
                boolean pawnStraight = targetPiece instanceof Pawn
                        && endX == board.getPieceCords(targetPiece)[0];
                if (targetPiece.getColor().equals(enemyColor) && !pawnStraight
                        && allowPieceMove(startX, startY, endX, endY, targetPiece)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean canPieceMoveHereAndBlockCheck(int endX, int endY, Color kingColor) {

        Piece targetPiece;

        for (int startX = 0; startX < 8; startX++) {
            for (int startY = 0; startY < 8; startY++) {

                targetPiece = board.getPieceFromCell(startX, startY);

                //pawn cannot block check with attacking move. King cannot block himself too
                if (targetPiece == null || (targetPiece instanceof Pawn &&
                        board.getPieceCords(targetPiece)[0] != endX) || targetPiece instanceof King) {
                    continue;
                }

                if (targetPiece.getColor().equals(kingColor) &&
                        allowPieceMove(startX, startY, endX, endY, targetPiece)) {
                    return true;
                }
            }
        }

        return false;
    }

    public ArrayList<Piece> findPiecesCheckingTheKing(King king) {

        Color enemyColor = king.getColor().equals(Color.WHITE) ? Color.BLACK : Color.WHITE;
        ArrayList<Piece> checkingPieces = new ArrayList<>(3);

        int kingX = board.getPieceCords(king)[0];
        int kingY = board.getPieceCords(king)[1];

        Piece targetPiece;

        for (int startX = 0; startX < 8; startX++) {
            for (int startY = 0; startY < 8; startY++) {
                targetPiece = board.getPieceFromCell(startX, startY);

                if (targetPiece == null || (targetPiece instanceof Pawn &&
                        board.getPieceCords(targetPiece)[0] == kingX)) {
                    continue;
                }
                if (targetPiece.getColor().equals(enemyColor) &&
                        allowPieceMove(startX, startY, kingX, kingY, targetPiece)) {
                    checkingPieces.add(targetPiece);
                }
            }
        }

        return checkingPieces;
    }


    public boolean checkCheckmate(King king, int kingPositionX, int kingPositionY) {

        ArrayList<Piece> attackingPieces = findPiecesCheckingTheKing(king);

        if (!king.isUnderCheck() || attackingPieces.isEmpty()) {
            return false;
        }

        ArrayList<Integer[]> cellsAroundKing = new ArrayList<>(10);

        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                if (x == 0 && y == 0) {
                    continue;
                }
                if (allowPieceMove(kingPositionX, kingPositionY,
                        (kingPositionX + x), (kingPositionY + y), king)) {
                    cellsAroundKing.add(new Integer[]{kingPositionX + x, kingPositionY + y});
                }
            }
        }

        for (Integer[] cell : cellsAroundKing) {
            if (allowPieceMove(kingPositionX, kingPositionY, cell[0], cell[1], king)) {
                return false;
            }
        }

        // Check if the king is in double check
        if (attackingPieces.size() == 2) {
            return true;
        }
        // Check if the attacking piece can be captured or blocked
        Piece attackingPiece = attackingPieces.get(0);
        int attackingX = board.getPieceCords(attackingPiece)[0];
        int attackingY = board.getPieceCords(attackingPiece)[1];

        if (canBePieceOfThisColorAttackedOnCell
                (attackingX, attackingY, attackingPiece.getColor())
                || allowPieceMove(kingPositionX, kingPositionY, attackingX, attackingY, king)) {
            return false;
        }

        if (attackingPiece instanceof Knight || attackingPiece instanceof Pawn) {
            return true;
        } else {
            List<Integer[]> cellsBetweenKingAndAttacker = findCellsBetweenKingAndAttacker(
                    attackingX, attackingY, kingPositionX, kingPositionY);

            for (Integer[] cell : cellsBetweenKingAndAttacker) {
                if (canPieceMoveHereAndBlockCheck(cell[0], cell[1], king.getColor())) {
                    return false;
                }
            }
        }
        return false;
    }

    private List<Integer[]> findCellsBetweenKingAndAttacker
            (int startX, int startY, int endX, int endY) {

        List<Integer[]> cellsBetween = new ArrayList<>();
        if (startX == endX || startY == endY) {
            if (startX == endX) {
                for (int i = Math.max(startY, endY); i > Math.min(startY, endY); i--) {
                    cellsBetween.add(new Integer[]{startX, i});
                }
            } else {
                for (int i = Math.max(startX, endX); i > Math.min(startX, endX); i--) {
                    cellsBetween.add(new Integer[]{i, startY});
                }
            }
        } else {
            int x = startX;
            int y = startY;
            int xStep = Integer.compare(endX, startX);
            int yStep = Integer.compare(endY, startY);

            //checking obstacles in the diagonal of movement
            while (x != endX || y != endY) {
                x += xStep;
                y += yStep;
                cellsBetween.add(new Integer[]{x, y});
            }
        }
        return cellsBetween;
    }

    public boolean checkIfProtectingKing(int pieceX, int pieceY, Piece piece, int kingX, int kingY) {

        boolean protecting;
        Color color = piece.getColor();

        board.hidePiece(pieceX, pieceY);
        protecting = checkIfDamagingKing(color, kingX, kingY);

        board.placePiece(pieceX, pieceY, piece);
        return protecting;
    }

    public boolean checkIfDamagingKing(Color kingColor, int kingX, int kingY) {

        Color enemyColor = kingColor.equals(Color.WHITE) ? Color.BLACK : Color.WHITE;
        Piece targetPiece;
        int startX, startY;

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {

                targetPiece = board.getPieceFromCell(x, y);
                if (targetPiece == null || !targetPiece.getColor().equals(enemyColor)) {
                    continue;
                }

                startX = board.getPieceCords(targetPiece)[0];
                startY = board.getPieceCords(targetPiece)[1];

                boolean straightAttack = allowStraightMove(startX, startY, kingX, kingY, targetPiece);
                boolean diagonalAttack = allowDiagonalMove(startX, startY, kingX, kingY);

                if (targetPiece instanceof Rook && straightAttack) {
                    return true;
                } else if (targetPiece instanceof Bishop && diagonalAttack) {
                    return true;
                } else if (targetPiece instanceof Queen && (diagonalAttack || straightAttack)) {
                    return true;
                }
            }
        }

        return false;
    }
}
