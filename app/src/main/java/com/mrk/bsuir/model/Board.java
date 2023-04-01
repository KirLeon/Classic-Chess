package com.mrk.bsuir.model;

import com.mrk.bsuir.model.impl.Bishop;
import com.mrk.bsuir.model.impl.King;
import com.mrk.bsuir.model.impl.Knight;
import com.mrk.bsuir.model.impl.Pawn;
import com.mrk.bsuir.model.impl.Queen;
import com.mrk.bsuir.model.impl.Rook;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Board {

    private Piece[][] boardCells;

    private static Board boardInstance;

    private List<Piece> piecesPositions;

    private Board() {

    }

    public static Board getBoardInstance() {
        Board instance = boardInstance;
        if (instance == null) {
            synchronized (Board.class) {
                instance = boardInstance;
                if (instance == null) {
                    boardInstance = new Board();
                    boardInstance.initBoard();
                    instance = boardInstance;
                }
            }
        }
        return instance;
    }

    public void initBoard() {

        //White pieces
        Stream<Piece> whitePieces = Stream.of(new Rook(Color.WHITE), new Knight(Color.WHITE),
                new Bishop(Color.WHITE), new Queen(Color.WHITE), new King(Color.WHITE),
                new Bishop(Color.WHITE), new Knight(Color.WHITE), new Rook(Color.WHITE));

        //Black pieces
        Stream<Piece> blackPieces = Stream.of(new Rook(Color.BLACK), new Knight(Color.BLACK),
                new Bishop(Color.BLACK), new Queen(Color.BLACK), new King(Color.BLACK),
                new Bishop(Color.BLACK), new Knight(Color.BLACK), new Rook(Color.BLACK));

        //Declaring the new board and assigning null to every cell
        boardCells = new Piece[7][7];
        Arrays.stream(boardCells)
                .forEach(pieces -> Arrays.stream(pieces)
                        .forEach(piece -> piece = null));

        //Filling board cells with pieces
        boardCells[0] = (Piece[]) whitePieces.toArray();
        Arrays.stream(boardCells[1]).forEach(piece -> piece = new Pawn(Color.WHITE));

        Arrays.stream(boardCells[6]).forEach(piece -> piece = new Pawn(Color.BLACK));
        boardCells[7] = (Piece[]) blackPieces.toArray();
    }

    public Piece getPieceFromCell(int x, int y) {
        return boardCells[x][y];
    }
}
