package com.mrk.bsuir.service;

public enum Action {

    CAPTURE("x"), CHECK("+"), SHORT_CASTLING("0-0"),
    LONG_CASTLING("0-0-0"), CHECKMATE("#"), PROMOTION("="),
    EN_PASSANT("e.p.");

    private final String loggingSymbol;

    public String getLoggingSymbol() {
        return loggingSymbol;
    }

    Action(String loggingSymbol) {
        this.loggingSymbol = loggingSymbol;
    }
}
