package com.mrk.bsuir.model;

public enum Color {

    WHITE("White"), BLACK("Black");

    private final String color;

    public String getColor() {
        return color;
    }

    Color(String color) {
        this.color = color;
    }
}
