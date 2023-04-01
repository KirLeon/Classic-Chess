package com.mrk.bsuir.logic;

public enum Action {
    BEATING(":"), CHECK("+"),
    SHORT_CASTLING("0-0"), LONG_CASTLING("0-0-0"),
    CHECKMATE("x"), PROMOTION("TQ");
    //TODO change the promotion symbols

    private String loggingSymbol;

    public String getLoggingSymbol() {
        return loggingSymbol;
    }

    Action(String loggingSymbol) {
        this.loggingSymbol = loggingSymbol;
    }
}
