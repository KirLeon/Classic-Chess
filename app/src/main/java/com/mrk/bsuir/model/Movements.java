package com.mrk.bsuir.model;

public enum Movements {

    PAWN_STRAIGHT(1), PAWN_ATTACK(1),

    DIAGONAL_MOVE(7), STRAIGHT_MOVE(7),

    KNIGHT_JUMP(2), KING_AROUND(1);


    private final int cellsRange;

    public int getCellsRange() {
        return cellsRange;
    }

    Movements(int cellsRange) {
        this.cellsRange = cellsRange;
    }
}
