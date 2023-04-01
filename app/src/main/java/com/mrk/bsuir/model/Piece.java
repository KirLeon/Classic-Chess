package com.mrk.bsuir.model;

public class Piece {

    protected final Color color;
    protected boolean protectingKing;

    public Piece(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
