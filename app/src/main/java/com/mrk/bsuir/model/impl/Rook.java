package com.mrk.bsuir.model.impl;

import com.mrk.bsuir.model.Color;
import com.mrk.bsuir.model.Movements;
import com.mrk.bsuir.model.Piece;

import java.util.Objects;

public class Rook extends Piece {

    private boolean firstMove = true;

    public Rook(Color color) {
        super(color);
    }

    public boolean isFirstMove() {
        return firstMove;
    }

    public void setFirstMove(boolean firstMove) {
        this.firstMove = firstMove;
    }

    public Rook(Color color, int startX, int startY) {
        super(color, startX, startY);
    }
}
