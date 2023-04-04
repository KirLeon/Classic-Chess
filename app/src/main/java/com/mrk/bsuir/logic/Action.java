package com.mrk.bsuir.logic;

public enum Action {

    CAPTURE("x"), CHECK("+"), EN_PASSANT(":"),
    SHORT_CASTLING("0-0"), LONG_CASTLING("0-0-0"),
    CHECKMATE("x"), PROMOTION("!");
    //TODO change the promotion symbols

    private String loggingSymbol;

    public String getLoggingSymbol() {
        return loggingSymbol;
    }

    Action(String loggingSymbol) {
        this.loggingSymbol = loggingSymbol;
    }
}
