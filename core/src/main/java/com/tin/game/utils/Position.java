package com.tin.game.utils;

public class Position {
    public final int row, col;

    public Position(int col, int row) {
        this.row = row;
        this.col = col;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position other = (Position) obj;
        return row == other.row && col == other.col;
    }

    @Override
    public int hashCode() {
        final int prime = 53;
        int result = 1;
        result = prime * result + this.col;
        result = prime * result + this.row;
        return result;
    }

    @Override
    public String toString() {
        return "(" + col + ", " + row + ")";
    }
}
