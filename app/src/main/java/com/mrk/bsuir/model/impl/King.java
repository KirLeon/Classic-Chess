package com.mrk.bsuir.model.impl;

import com.mrk.bsuir.model.Color;
import com.mrk.bsuir.model.Piece;

public class King extends Piece {

    private boolean firstMove = true;
    private boolean isUnderCheck = false;

    public King(Color color) {
        super(color);
    }

    public King(Color color, int startX, int startY) {
        super(color, startX, startY);
    }

    public boolean isFirstMove() {
        return firstMove;
    }

    public void setFirstMove(boolean firstMove) {
        this.firstMove = firstMove;
    }

    public boolean isUnderCheck() {
        return isUnderCheck;
    }

    public void setUnderCheck(boolean underCheck) {
        isUnderCheck = underCheck;
    }
}
