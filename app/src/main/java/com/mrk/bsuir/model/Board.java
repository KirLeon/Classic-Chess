package com.mrk.bsuir.model;

import android.util.Log;

import com.mrk.bsuir.logic.MoveService;
import com.mrk.bsuir.model.impl.Bishop;
import com.mrk.bsuir.model.impl.King;
import com.mrk.bsuir.model.impl.Knight;
import com.mrk.bsuir.model.impl.Pawn;
import com.mrk.bsuir.model.impl.Queen;
import com.mrk.bsuir.model.impl.Rook;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Board {

    private Piece[][] boardCells;
    private MoveService moveService;
    private Piece currentPiece = null;

    public Board() {
        initBoard();
    }

    public MoveService getMoveService() {
        return moveService;
    }

    public void setMoveService(MoveService moveService) {
        this.moveService = moveService;
    }

    public void initBoard() {

        //White pieces
        Piece[] whitePieces = Stream.of(new Rook(Color.WHITE), new Knight(Color.WHITE),
                        new Bishop(Color.WHITE), new Queen(Color.WHITE), new King(Color.WHITE),
                        new Bishop(Color.WHITE), new Knight(Color.WHITE), new Rook(Color.WHITE))
                .toArray(Piece[]::new);


        //Black pieces
        Piece[] blackPieces = Stream.of(new Rook(Color.BLACK), new Knight(Color.BLACK),
                        new Bishop(Color.BLACK), new Queen(Color.BLACK), new King(Color.BLACK),
                        new Bishop(Color.BLACK), new Knight(Color.BLACK), new Rook(Color.BLACK))
                .toArray(Piece[]::new);

        //Declaring the new board and assigning null to every cell
        boardCells = new Piece[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                boardCells[i][j] = null;
            }
        }

        //Filling board cells with pieces
        IntStream.range(0, 8)
                .forEach(i -> {
                    boardCells[i][0] = whitePieces[i];
                    boardCells[i][7] = blackPieces[i];
                    boardCells[i][1] = new Pawn(Color.WHITE);
                    boardCells[i][6] = new Pawn(Color.BLACK);
                });

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                if(getPieceFromCell(i, j) == null) continue;
                getPieceFromCell(i, j).setStartPosition(i, j);
            }
        }
//
//        for (int a = 0; a < 7; a++) {
//            for (int b = 0; b < 7; b++) {
//                Log.i("Board", "Piece on " + a + "" + b + " is " + getPieceFromCell(a,b));
//            }
//        }
    }

    public Color getCellColor(int x, int y) {
        return (x + y) % 2 == 0 ? Color.BLACK : Color.WHITE;
    }

    public Piece getPieceFromCell(int x, int y) {
        return boardCells[x][y];
    }

    public boolean isCellUnderAttackForColor(int endX, int endY, Color color) {

        Color enemyColor = color.equals(Color.WHITE) ? Color.BLACK : Color.WHITE;
        Piece targetPiece;

        for (int startX = 0; startX < 7; endX++) {
            for (int startY = 0; startY < 7; startY++) {
                targetPiece = getPieceFromCell(startX, startY);
                if (targetPiece != null && targetPiece.getColor().equals(enemyColor)
                        && moveService.allowPieceMove(startX, startY, endX, endY, targetPiece)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void movePiece(int startX, int startY, int endX, int endY, Piece piece) {
        boardCells[startX][startY] = null;
        boardCells[endX][endY] = piece;
    }

    public Piece getCurrentPiece() {
        return currentPiece;
    }

    public void setCurrentPiece(Piece currentPiece) {
        this.currentPiece = currentPiece;
    }

    public int[] getPieceCords(Piece piece) {
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 8; j++) {
                if (piece.equals(getPieceFromCell(i, j))) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

}
