package com.mrk.bsuir.model.impl;

import com.mrk.bsuir.model.Color;
import com.mrk.bsuir.model.Movements;
import com.mrk.bsuir.model.Piece;

public class Pawn extends Piece {

    private boolean firstMove = true;

    public Pawn(Color color) {
        super(color);
        allowedMoves.add(Movements.PAWN_STRAIGHT);
        allowedMoves.add(Movements.PAWN_ATTACK);
    }

    public boolean isFirstMove() {
        return firstMove;
    }

    public void setFirstMove(boolean firstMove) {
        this.firstMove = firstMove;
    }
}
