package com.mrk.bsuir.model;

import com.mrk.bsuir.model.impl.Bishop;
import com.mrk.bsuir.model.impl.King;
import com.mrk.bsuir.model.impl.Knight;
import com.mrk.bsuir.model.impl.Pawn;
import com.mrk.bsuir.model.impl.Queen;
import com.mrk.bsuir.model.impl.Rook;

import java.util.stream.IntStream;

public class Board {

    private Piece[][] boardCells;
    private Piece currentPiece = null;

    private King whiteKing = null;
    private King blackKing = null;

    public Board() {
        initBoard();
    }

    public void initBoard() {

        whiteKing = new King(Color.WHITE, 4, 0);
        blackKing = new King(Color.BLACK, 4, 7);

        // White pieces
        Piece[] whitePieces = new Piece[]{
                new Rook(Color.WHITE, 0, 0),
                new Knight(Color.WHITE, 1, 0),
                new Bishop(Color.WHITE, 2, 0),
                new Queen(Color.WHITE, 3, 0),
                whiteKing,
                new Bishop(Color.WHITE, 5, 0),
                new Knight(Color.WHITE, 6, 0),
                new Rook(Color.WHITE, 7, 0)
        };

        // Black pieces
        Piece[] blackPieces = new Piece[]{
                new Rook(Color.BLACK, 0, 7),
                new Knight(Color.BLACK, 1, 7),
                new Bishop(Color.BLACK, 2, 7),
                new Queen(Color.BLACK, 3, 7),
                blackKing,
                new Bishop(Color.BLACK, 5, 7),
                new Knight(Color.BLACK, 6, 7),
                new Rook(Color.BLACK, 7, 7)
        };

        // Declaring the new board and assigning null to every cell
        boardCells = new Piece[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                boardCells[i][j] = null;
            }
        }

        // Filling board cells with pieces
        IntStream.range(0, 8)
                .forEach(i -> {
                    boardCells[i][0] = whitePieces[i];
                    boardCells[i][7] = blackPieces[i];
                    boardCells[i][1] = new Pawn(Color.WHITE, i, 1);
                    boardCells[i][1].setStartPosition(i,1);
                    boardCells[i][6] = new Pawn(Color.BLACK, i, 6);
                    boardCells[i][6].setStartPosition(i,6);
                });
    }

    public Color getCellColor(int x, int y) {
        return (x + y) % 2 == 0 ? Color.BLACK : Color.WHITE;
    }

    public Piece getPieceFromCell(int x, int y) {
        return boardCells[x][y];
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
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (piece.equals(getPieceFromCell(i, j))) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }


    public King getKingOfThisColor(Color color){
        return color.equals(Color.WHITE) ? whiteKing : blackKing;
    }

    public King getKingOfAnotherColor(Color color){
        return color.equals(Color.WHITE) ? blackKing : whiteKing;
    }

    public void placePiece(int x, int y, Piece piece){
        boardCells[x][y] = piece;
    }

}
