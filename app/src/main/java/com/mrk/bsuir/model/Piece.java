package com.mrk.bsuir.model;

import java.util.List;

public class Piece {

    protected final Color color;
    protected boolean protectingKing;
    protected List<Movements> allowedMoves;

    public Piece(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public boolean isProtectingKing() {
        return protectingKing;
    }

    public void setProtectingKing(boolean protectingKing) {
        this.protectingKing = protectingKing;
    }

    public List<Movements> getAllowedMoves() {
        return allowedMoves;
    }

    public void setAllowedMoves(List<Movements> allowedMoves) {
        this.allowedMoves = allowedMoves;
    }
}
