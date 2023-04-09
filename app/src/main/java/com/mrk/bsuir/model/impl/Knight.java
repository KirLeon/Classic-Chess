package com.mrk.bsuir.model.impl;

import com.mrk.bsuir.model.Color;
import com.mrk.bsuir.model.Movements;
import com.mrk.bsuir.model.Piece;

public class Knight extends Piece {

    public Knight(Color color) {
        super(color);
    }

    public Knight(Color color, int startX, int startY) {
        super(color, startX, startY);
    }
}
