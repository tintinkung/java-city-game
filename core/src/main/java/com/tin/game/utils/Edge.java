package com.tin.game.utils;

/**
 * Undirected edge of pos1 and pos2 where
 * (pos1, pos2) is {@link Edge#equals} to (pos2, pos1)
 */
public class Edge {

    public final Position pos1;
    public final Position pos2;

    public Edge(Position pos1, Position pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public Edge(int col1, int row1, int col2, int row2) {
        this.pos1 = new Position(col1, row1);
        this.pos2 = new Position(col2, row2);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Edge other = (Edge) obj;

        return (pos1.equals(other.pos1) && pos2.equals(other.pos2))
            || (pos1.equals(other.pos2) && pos2.equals(other.pos1));
    }

    @Override
    public int hashCode() {
        return pos1.hashCode() + pos2.hashCode();
    }

    @Override
    public String toString() {
        return pos1 + " -> " + pos2;
    }
}
