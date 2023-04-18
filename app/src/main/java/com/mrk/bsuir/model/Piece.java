package com.mrk.bsuir.model;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Objects;

public abstract class Piece{

    protected final Color color;

    //TODO make different coordinated for promoted pieces
    protected final int[] startPosition;

    public Piece(Color color) {
        this.color = color;
        startPosition = new int[2];
        setStartPosition(-1, -1);
    }

    public Piece(Color color, int startX, int startY) {
        this.color = color;
        startPosition = new int[2];
        setStartPosition(startX, startY);
    }

    public Color getColor() {
        return color;
    }

    public void setStartPosition(int x, int y) {
        startPosition[0] = x;
        startPosition[1] = y;
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

    @NonNull
    @Override
    public String toString() {
        return color.getColor() + " " + getClass().getName();
    }

}
