package com.mrk.bsuir.model.impl;

import com.mrk.bsuir.model.Color;
import com.mrk.bsuir.model.Movements;
import com.mrk.bsuir.model.Piece;

public class Bishop extends Piece {

    public Bishop(Color color) {
        super(color);
        allowedMoves.add(Movements.DIAGONAL_MOVE);
    }

}
