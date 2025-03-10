package com.tin.game.utils;

public class Node<T extends SimplePath> implements Comparable<Node<T>> {
    public final Position position;
    public final T viaSubPath;
    public final int cost;

    public  Node(Position position, int cost, T viaSubPath) {
        this.position = position;
        this.cost = cost;
        this.viaSubPath = viaSubPath;
    }

    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.cost, other.cost);
    }
}
