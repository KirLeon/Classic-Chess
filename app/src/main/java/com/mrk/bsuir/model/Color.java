package com.mrk.bsuir.model;

public enum Color {

    BLACK("Black"), WHITE("White");

    private String color;

    public String getColor() {
        return color;
    }

    Color(String color) {
        this.color = color;
    }
}
