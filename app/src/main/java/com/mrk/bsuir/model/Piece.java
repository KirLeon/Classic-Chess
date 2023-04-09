package com.mrk.bsuir.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Piece {

    protected final Color color;
    protected boolean protectingKing;
    protected int[] startPosition;
    protected boolean isChecking = false;

    public Piece(Color color) {
        this.color = color;
        startPosition = new int[2];
    }

    public Piece(Color color, int startX, int startY) {
        this.color = color;
        startPosition = new int[2];
        setStartPosition(startX, startY);
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

    public Piece setStartPosition(int x, int y){
        startPosition[0] = x;
        startPosition[1] = y;
        return this;
    }

    public boolean isChecking() {
        return isChecking;
    }

    public void setChecking(boolean checking) {
        isChecking = checking;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Piece piece = (Piece) o;
        return color == piece.color && Arrays.equals(startPosition, piece.startPosition);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(color);
        result = 31 * result + Arrays.hashCode(startPosition);
        return result;
    }

    @Override
    public String toString() {
        return color.getColor() + " " + getClass().getName();
    }
}
