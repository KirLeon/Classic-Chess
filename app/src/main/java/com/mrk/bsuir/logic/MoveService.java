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

/**
 * This class is designed to test the possibility of a particular move.
 * All the basic logic is concentrated here and this class is of paramount importance.
 * Validation of any moves, check check, transformation, checkmate, etc. — this is exactly the task
 * of MoveService.
 * In case of errors and finding bugs, I strongly recommend looking for the reasons here.
 */

public class MoveService {

    private final Board board;
    private final GameLogService logService;
    private final String REGEX_MOVE_PATTERN = "^P[0-7][0-7][0-7][0-7]$";

    public MoveService(Board board, GameLogService logService) {
        this.board = board;
        this.logService = logService;
    }

    // Defining valid coordinates for any piece
    private final int minAllowableX = 0;
    private final int maxAllowableX = 7;


    /**
     * This method checks the availability of the move.
     */
    public boolean allowPieceMove(int startX, int startY, int endX, int endY, Piece piece) {

        //checking coordinates
        if (startX < minAllowableX || endX > maxAllowableX || startY < 0 || endY > 7) {
            return false;
        }

        if (startX == endX && startY == endY) {
            return false;
        }

        // Check is piece protecting the king
        if (!(piece instanceof King)) {

            King king = board.getKingOfThisColor(piece.getColor());
            int kingX = board.getPieceCords(king)[0];
            int kingY = board.getPieceCords(king)[1];

            if (checkIfProtectingKing(startX, startY, piece, kingX, kingY)) {
                return false;
            }
        }

        boolean validMove;
        if (board.getPieceFromCell(endX, endY) != null &&
                board.getPieceFromCell(endX, endY).getColor().equals(piece.getColor())) {
            return false;
        }

        if (piece instanceof Pawn) {
            validMove = allowPawnStraightMove(startX, startY, endX, endY, (Pawn) piece);
        } else if (piece instanceof Bishop) {
            validMove = allowDiagonalMove(startX, startY, endX, endY);
        } else if (piece instanceof Knight) {
            validMove = allowKnightJump(startX, startY, endX, endY);
        } else if (piece instanceof Rook) {
            validMove = allowStraightMove(startX, startY, endX, endY, piece);
        } else if (piece instanceof Queen) {
            validMove = allowStraightMove(startX, startY, endX, endY, piece) ||
                    allowDiagonalMove(startX, startY, endX, endY);
        } else if (piece instanceof King) {
            validMove = allowKingAroundMove(startX, startY, endX, endY, (King) piece);
        } else throw new RuntimeException("Incorrect piece");

        return validMove;
    }

    /**
     * This method checks the availability of every pawn move, made from player. Some possibilities
     * of the moves are checking in such methods as isThisCellUnderAttack, canBeThisCellCapturedBy,
     * e.t.c., and they using only allowPawnAttackMove (canPieceBlockCheckHere is an exception for
     * this rule). So it is called from allowPieceMove and calls enPassant or attackMove if required.
     */
    public boolean allowPawnStraightMove(int startX, int startY, int endX, int endY, Pawn pawn) {

        Color color = pawn.getColor();
        boolean isFirstMove = pawn.isFirstMove();
        int xDiff = Math.abs(startX - endX);
        int yDiff = Math.abs(startY - endY);

        //Checking for correct direction of pawn movement, allowed amount of crossed cells
        if (xDiff > 1) return false;

        int direction = color == Color.WHITE ? 1 : -1;
        boolean longMove = isFirstMove && endY - startY == 2 * direction &&
                board.getPieceFromCell(startX, startY + direction) == null;

        //if piece's 1 cells straight move or 2 cells long move is made in wrong direction
        if (endY - startY != direction && !(longMove)) {
            return false;
        }

        //Case horizontal cell changed check the ability to attack
        else if (xDiff == 1) {

            if (yDiff != 1) return false;

            if (board.getPieceFromCell(endX, endY) == null) {
                return allowEnPassant(endX, endY, pawn);
            } else {
                return allowPawnAttackMove(startX, startY, endX, endY, pawn);
            }
        }


        //if something blocking the way
        if (board.getPieceFromCell(endX, endY) != null) return false;

        //Pawn can move 2 cells forward only in case it's moving first time


        pawn.setFirstMove(false);
        return true;
    }

    /**
     * This method checks the availability of the en passant (when a pawn captures a horizontally
     * adjacent enemy pawn that has just made an initial two-square advance).
     */
    public boolean allowEnPassant(int endX, int endY, Pawn pawn) {

        String lastMove = logService.getLastMove();
        if (lastMove.matches(REGEX_MOVE_PATTERN)) {

            int lastMoveStartX = lastMove.charAt(1);
            int lastMoveStartY = lastMove.charAt(2);
            int lastMoveEndX = lastMove.charAt(3);
            int lastMoveEndY = lastMove.charAt(4);

            //in case this move was from white, the middle cord is StartY + 1, else vice versa
            int middleCell = lastMoveEndY > lastMoveStartY ? lastMoveStartY + 1
                    : lastMoveStartY - 1;

            //checking firstMove of enemy pawn and correct capturing of it
            if (Math.abs(lastMoveEndY - lastMoveStartY) == 2 && endY == middleCell &&
                    lastMoveStartX == lastMoveEndX && lastMoveEndX == endX) {

                pawn.setFirstMove(false);
                return true;
            }

        }
        return false;
    }

    /**
     * This method checks the availability of the pawn attack move.
     */
    public boolean allowPawnAttackMove(int startX, int startY, int endX, int endY, Pawn pawn) {

        int direction = pawn.getColor() == Color.WHITE ? 1 : -1;
        return endY - startY == direction && Math.abs(startX - endX) == 1;
    }

    /**
     * This method checks the availability of the diagonal move, required by bishop and queen.
     */
    public boolean allowDiagonalMove(int startX, int startY, int endX, int endY) {

        // Color of the cell should be the same and delta of (x,y) coordinates should be same too
        if (!board.getCellColor(startX, startY).equals(board.getCellColor(endX, endY)) ||
                Math.abs(startX - endX) != Math.abs(startY - endY)) {
            return false;
        }

        int xStep = endX - startX > 0 ? 1 : -1;
        int yStep = endY - startY > 0 ? 1 : -1;
        int iterations = Math.abs(startX - endX);

        // Checking obstacles in the diagonal of movement
        for (int i = 1; i < iterations; i++) {
            if (board.getPieceFromCell(startX + i * xStep, startY + i * yStep) != null) {
                return false;
            }
        }

        return true;
    }

    /**
     * This method checks the availability of the diagonal move, required by rook and queen.
     */
    public boolean allowStraightMove(int startX, int startY, int endX, int endY, Piece piece) {

        // Only one of lines (horizontal or vertical) is allowed to change
        if (Math.abs(startX - endX) != 0 && Math.abs(startY - endY) != 0) {
            return false;
        }

        // Checking obstacles in the line of movement
        if (startX == endX) {
            for (int i = Math.max(startY, endY) - 1; i > Math.min(startY, endY); i--) {
                if (board.getPieceFromCell(endX, i) != null) return false;
            }
        } else {
            for (int i = Math.max(startX, endX) - 1; i > Math.min(startX, endX); i--) {
                if (board.getPieceFromCell(i, endY) != null) return false;
            }
        }

        // For canceling the ability of castling after rook's first move
        if (piece instanceof Rook) {
            ((Rook) piece).setFirstMove(false);
        }

        return true;
    }

    /**
     * This method checks the availability of king move. Important is it not only calls castling move
     * if required, but it uses isThisCellUnderAttack method to check king's possibility to move on
     * some cell. We should not use canBeThisCellCapturedBy method, because it will provide
     * difficulties with enemy king's attack's zone and can lead to recursive calls.
     */
    public boolean allowKingAroundMove(int startX, int startY, int endX, int endY, King king) {

        int xDiff = Math.abs(startX - endX);
        int yDiff = Math.abs(startY - endY);

        // If the move is a castling move, check if it is allowed
        if (xDiff == 2)
            return allowCastling(endX, endY, king);

            // Otherwise, check if the move is valid
            // Important: use isUnderAtt... instead of canBeCapt... due to king's check possibility

        else if (xDiff > 1 || yDiff > 1 || board.getPieceFromCell(endX, endY) != null
                || isThisCellUnderAttack(endX, endY, king.getColor())) {
            return false;
        }

        king.setFirstMove(false);
        return true;

    }

    /**
     * This method checks the availability of castling. It checks possibility to make castling through
     * inspecting enemy ability to attack some cells on king's way to it's end position. Important is
     * that this method uses isThisCellUnderAttack instead of canBeThisCellCapturedBy due to reasons,
     * described above.
     */
    public boolean allowCastling(int endX, int endY, King king) {

        // Cannot castle while king is under check or it is not his first move
        if (!king.isFirstMove() || king.isUnderCheck()) return false;

        int firstStepCellX = 4 - Integer.compare(4, endX);
        int secondStepCellX = 4 - (Integer.compare(4, endX) * 2);
        int rookCellX = 4 > endX ? 0 : 7;

        Piece piece = board.getPieceFromCell(rookCellX, endY);
        if (!(piece instanceof Rook) || !((Rook) piece).isFirstMove()
                || !piece.getColor().equals(king.getColor())) {
            return false;
        }

        //checking attack on cells between king's start and end x coordinate
        return board.getPieceFromCell(firstStepCellX, endY) == null
                && board.getPieceFromCell(secondStepCellX, endY) == null
                && !isThisCellUnderAttack(firstStepCellX, endY, king.getColor())
                && !isThisCellUnderAttack(secondStepCellX, endY, king.getColor());
    }

    private boolean allowKnightJump(int startX, int startY, int endX, int endY) {

        int xDiff = Math.abs(startX - endX);
        int yDiff = Math.abs(startY - endY);

        return xDiff + yDiff == 3 && xDiff >= 1 && yDiff >= 1;
    }

    // This method checks only attack range of the piece excluding checking the ability to move here
    public boolean isThisCellUnderAttack(int endX, int endY, Color kingColor) {

        Piece targetPiece;

        for (int startX = 0; startX < 8; startX++) {
            for (int startY = 0; startY < 8; startY++) {

                targetPiece = board.getPieceFromCell(startX, startY);
                if (targetPiece == null || targetPiece.getColor().equals(kingColor)) {
                    continue;
                }
                startX = board.getPieceCords(targetPiece)[0];
                startY = board.getPieceCords(targetPiece)[1];

                if (targetPiece instanceof Pawn) {
                    if (allowPawnAttackMove(startX, startY, endX, endY, (Pawn) targetPiece)) {
                        return true;
                    }
                } else if (targetPiece instanceof Knight) {
                    if (allowKnightJump(startX, startY, endX, endY)) {
                        return true;
                    }
                } else if (targetPiece instanceof King) {
                    if (Math.abs(startX - endX) < 2 && Math.abs(startY - endY) < 2) {
                        return true;
                    }
                }

                boolean straightAttack = allowStraightMove(startX, startY, endX, endY, targetPiece);
                boolean diagonalAttack = allowDiagonalMove(startX, startY, endX, endY);

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

    public boolean canBeThisCellCapturedBy(int endX, int endY, Color kingColor) {

        Color enemyColor = kingColor.equals(Color.WHITE) ? Color.BLACK : Color.WHITE;
        Piece targetPiece;

        for (int startX = 0; startX < 8; startX++) {
            for (int startY = 0; startY < 8; startY++) {

                targetPiece = board.getPieceFromCell(startX, startY);
                if (targetPiece == null) {
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

    /**
     * This method checks the fact of some piece protecting the king. It works next way:
     * 1. Makes piece's position on the board null ( = piece disappears).
     * 2. Inspects for the ability of bishop, rook and queen to check the king.
     * It's never called during king's under check, so it shows the difference between pieces
     * presence on some cell and it's absence.
     */
    public boolean checkIfProtectingKing(int pieceX, int pieceY, Piece piece, int kingX, int kingY) {

        boolean protecting;
        Color color = piece.getColor();

        board.hidePiece(pieceX, pieceY);
        protecting = checkIfDamagingKing(color, kingX, kingY);

        board.placePiece(pieceX, pieceY, piece);
        return protecting;
    }

    /**
     * This method checks the possibility of checking the king for bishop, rook and queen for the
     * above method.
     */

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

    public boolean canPieceBlockCheckHere(int endX, int endY, Color kingColor) {

        Piece targetPiece;
        for (int startX = 0; startX < 8; startX++) {
            for (int startY = 0; startY < 8; startY++) {

                targetPiece = board.getPieceFromCell(startX, startY);

                // Pawn cannot block check with attacking move; King cannot block himself too
                if (targetPiece == null || targetPiece instanceof King ||
                        (targetPiece instanceof Pawn &&
                                Math.abs(board.getPieceCords(targetPiece)[0] - endX) != 0)) {
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

        ArrayList<Piece> checkingPieces = new ArrayList<>(3);
        int checkingPiecesAmount = 0;

        int kingX = board.getPieceCords(king)[0];
        int kingY = board.getPieceCords(king)[1];

        Piece targetPiece;

        for (int startX = 0; startX < 8; startX++) {
            for (int startY = 0; startY < 8; startY++) {

                if (checkingPiecesAmount > 1) {
                    return checkingPieces;
                }

                targetPiece = board.getPieceFromCell(startX, startY);
                if (targetPiece == null || targetPiece.getColor().equals(king.getColor())) {
                    continue;
                }

                if (startX == 5 && startY == 6) {
                    System.out.println("here");
                }


                startX = board.getPieceCords(targetPiece)[0];
                startY = board.getPieceCords(targetPiece)[1];

                if (targetPiece instanceof Pawn) {
                    if (allowPawnAttackMove(startX, startY, kingX, kingY, (Pawn) targetPiece)) {
                        checkingPieces.add(targetPiece);
                        checkingPiecesAmount++;
                        continue;
                    }
                } else if (targetPiece instanceof Knight) {
                    if (allowKnightJump(startX, startY, kingX, kingY)) {
                        checkingPieces.add(targetPiece);
                        checkingPiecesAmount++;
                        continue;
                    }
                } else if (targetPiece instanceof King) {
                    continue;
                }

                boolean straightAttack = allowStraightMove(startX, startY, kingX, kingY, targetPiece);
                boolean diagonalAttack = allowDiagonalMove(startX, startY, kingX, kingY);

                if (targetPiece instanceof Rook && straightAttack) {
                    checkingPieces.add(targetPiece);
                    checkingPiecesAmount++;
                } else if (targetPiece instanceof Bishop && diagonalAttack) {
                    checkingPieces.add(targetPiece);
                    checkingPiecesAmount++;
                } else if (targetPiece instanceof Queen && (diagonalAttack || straightAttack)) {
                    checkingPieces.add(targetPiece);
                    checkingPiecesAmount++;
                }

            }
        }
        return checkingPieces;
    }

    public boolean onCheckAvailableMove(int startX, int startY, int endX, int endY, Piece piece,
                                        King king) {

        if (!allowPieceMove(startX, startY, endX, endY, piece)) {
            return false;
        }

        Piece tempPiece = board.getPieceFromCell(endX, endY);
        board.placePiece(endX, endY, piece);
        board.placePiece(startX, startY, null);

        int kingX = board.getPieceCords(king)[0];
        int kingY = board.getPieceCords(king)[1];

        boolean availableMove = !isThisCellUnderAttack(kingX, kingY, king.getColor());

        if(!availableMove){
            board.placePiece(endX, endY, tempPiece);
            board.placePiece(startX, startY, piece);
        }

        return availableMove;

    }

    private List<Integer[]> findCellsBetweenKingAndAttacker
            (int startX, int startY, int endX, int endY) {

        List<Integer[]> cellsBetween = new ArrayList<>();
        if (Math.abs(startX - endX) < 2 && Math.abs(startY - endY) < 2) {
            return cellsBetween;
        }

        if (startX == endX || startY == endY) {
            if (startX == endX) {
                for (int i = Math.max(startY, endY) - 1; i > Math.min(startY, endY); i--) {
                    cellsBetween.add(new Integer[]{startX, i});
                }
            } else {
                for (int i = Math.max(startX, endX) - 1; i > Math.min(startX, endX); i--) {
                    cellsBetween.add(new Integer[]{i, startY});
                }
            }
        } else {
            int x = startX;
            int y = startY;
            int xStep = Integer.compare(endX, startX);
            int yStep = Integer.compare(endY, startY);

            x += xStep;
            y += yStep;
            //checking obstacles in the diagonal of movement
            while (x != endX || y != endY) {
                x += xStep;
                y += yStep;
                cellsBetween.add(new Integer[]{x, y});
            }
        }
        return cellsBetween;
    }

    public boolean checkCheckmate(King king) {

        int kingPositionX = board.getPieceCords(king)[0];
        int kingPositionY = board.getPieceCords(king)[1];

        ArrayList<Piece> attackingPieces = findPiecesCheckingTheKing(king);
        for (Piece piece : attackingPieces) {
            System.out.println(piece);
        }
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

        // Check if attacking piece can be captured or blocked
        Piece attackingPiece = attackingPieces.get(0);
        int attackingX = board.getPieceCords(attackingPiece)[0];
        int attackingY = board.getPieceCords(attackingPiece)[1];

        if (canBeThisCellCapturedBy(attackingX, attackingY, attackingPiece.getColor())) {
            return false;
        }

        if (attackingPiece instanceof Knight || attackingPiece instanceof Pawn) {
            return true;
        } else {
            List<Integer[]> cellsBetweenKingAndAttacker = findCellsBetweenKingAndAttacker(
                    attackingX, attackingY, kingPositionX, kingPositionY);

            for (Integer[] cell : cellsBetweenKingAndAttacker) {
                if (canPieceBlockCheckHere(cell[0], cell[1], king.getColor())) {
                    return false;
                }
            }
        }
        return true;
    }

}
