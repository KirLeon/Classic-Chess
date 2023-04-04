package com.mrk.bsuir.model.impl;

import com.mrk.bsuir.model.Color;
import com.mrk.bsuir.model.Movements;
import com.mrk.bsuir.model.Piece;

public class Rook extends Piece {

    private boolean firstMove = true;

    public Rook(Color color) {
        super(color);
        allowedMoves.add(Movements.STRAIGHT_MOVE);
    }

    public boolean isFirstMove() {
        return firstMove;
    }

    public void setFirstMove(boolean firstMove) {
        this.firstMove = firstMove;
    }
}
