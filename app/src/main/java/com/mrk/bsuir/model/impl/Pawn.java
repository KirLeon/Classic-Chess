package com.mrk.bsuir.model.impl;

import com.mrk.bsuir.model.Color;
import com.mrk.bsuir.model.Piece;

public class Pawn extends Piece {

    private boolean firstMove = true;

    public Pawn(Color color, int startX, int startY) {
        super(color, startX, startY);
    }

    public boolean isFirstMove() {
        return firstMove;
    }

    public void setFirstMove(boolean firstMove) {
        this.firstMove = firstMove;
    }
}
