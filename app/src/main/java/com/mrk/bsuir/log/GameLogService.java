package com.mrk.bsuir.log;

import com.mrk.bsuir.logic.Action;
import com.mrk.bsuir.model.Piece;
import com.mrk.bsuir.model.impl.Bishop;
import com.mrk.bsuir.model.impl.King;
import com.mrk.bsuir.model.impl.Knight;
import com.mrk.bsuir.model.impl.Pawn;
import com.mrk.bsuir.model.impl.Queen;
import com.mrk.bsuir.model.impl.Rook;

import java.util.HashMap;
import java.util.Map;

public class GameLogService {

    private Map<Integer, String> gameMovesRecord;
    private Map<String, Class<? extends Piece>> piecesSymbols;
    private int currentMove;


    public GameLogService() {
        currentMove = 0;
        piecesSymbols = new HashMap<>();
        gameMovesRecord = new HashMap<>();
        piecesSymbols.put("P", Pawn.class);
        piecesSymbols.put("N", Knight.class);
        piecesSymbols.put("B", Bishop.class);
        piecesSymbols.put("R", Rook.class);
        piecesSymbols.put("Q", Queen.class);
        piecesSymbols.put("K", King.class);
    }

    public void logMove(int startX, int startY, int endX, int endY, Piece movingPiece, Action action) {
        //TODO make move logging
        currentMove++;
    }

    public String getMoveByNumber(int moveNumber){
        return gameMovesRecord.get(moveNumber);
    }

    public void parseMoveFromLog(String logLine, int moveNumber) {

        if(logLine == "0-0-0"){

        }

        Piece movedPiece;
        int startX;
        int startY;
        int endX;
        int endY;
        Piece beatenPiece;

        try {
            movedPiece = piecesSymbols.get("K").newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException("Error in game log: cannot parse " + logLine);
        }

    }

    public String getLastMove(){
        if(currentMove == 1) return null;
        return gameMovesRecord.get(currentMove - 2);
    }

    public int getCurrentMove() {
        return currentMove;
    }

    public void setCurrentMove(int currentMove) {
        this.currentMove = currentMove;
    }

}
