package com.chronicorn.frontend.contants;

public enum Direction {
    LEFT(2),
    RIGHT(4),
    DOWN(6),
    UP(8);

    public final int inputCode;

    private Direction(int inputCode) {
        this.inputCode = inputCode;
    }

    public static Direction fromInputCode(int code) {
        for (Direction d : values()) {
            if (d.inputCode == code) {
                return d;
            }
        }
        return DOWN; // Always return a safe default if the key is invalid
    }
}
