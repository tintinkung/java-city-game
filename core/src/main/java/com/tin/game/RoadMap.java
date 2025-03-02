package com.tin.game;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;

import java.util.Objects;


public class RoadMap {

    /**
     * The adjacency list of the entire road graph
     */
    private final ObjectMap<Position, AdjacencySet> roadMap;

    RoadMap() {
        roadMap = new ObjectMap<>();
    }

    private void put(Position position, AdjacencySet adjacency) {
        roadMap.put(position, adjacency);
    }

    private void put(int col, int row, AdjacencySet adjacency) {
        roadMap.put(new Position(col, row), adjacency);
    }

    private void putIfAbsent(Position position, AdjacencySet adjacency) {
        if(hasRoad(position)) return;
        put(position, adjacency);
    }

    private void putIfAbsent(int col, int row, AdjacencySet adjacency) {
        if(hasRoad(col, row)) return;
        put(col, row, adjacency);
    }

    private void putIfAbsent(MapCell cell, AdjacencySet adjacency) {
        if(hasRoad(cell.getPosition())) return;
        cell.setType(MapCell.CELL_TYPE.ROAD);
        put(cell.getPosition(), adjacency);
    }

    public void pushRoad(MapCell from, MapCell to) {
        putIfAbsent(from, new AdjacencySet());
        putIfAbsent(to, new AdjacencySet());

        getAdjacency(from.getPosition()).add(to);
        getAdjacency(to.getPosition()).add(from);
    }

    public void removeRoad(MapCell cell) {
        AdjacencySet adjacency = getAdjacency(cell.getPosition());

        // clear connections
        adjacency.forEach((adj) -> {
            getAdjacency(adj.getPosition()).remove(cell);
        });

        //delete itself
        remove(adjacency);
    }

    private void remove(Position at) {
        roadMap.remove(at);
    }

    private void remove(int col, int row) {
        roadMap.remove(new Position(col, row));
    }

    private void remove(AdjacencySet adjacency) {
        roadMap.remove(roadMap.findKey(adjacency, false));
    }

    public AdjacencySet getAdjacency(int col, int row) {
        return roadMap.get(new Position(col, row));
    }


    public AdjacencySet getAdjacency(Position position) {
        return roadMap.get(position);
    }

    public AdjacencySet getAdjacency(int col, int row, AdjacencySet defaultValue) {
        return roadMap.get(new Position(col, row), defaultValue);
    }

    public AdjacencySet getAdjacency(Position position, AdjacencySet defaultValue) {
        return roadMap.get(position, defaultValue);
    }

    public boolean hasRoad(int col, int row) {
        return roadMap.containsKey(new Position(col, row));
    }

    public boolean hasRoad(Position position) {
        return roadMap.containsKey(position);
    }

    public boolean isEmpty() {
        return roadMap.isEmpty();
    }

    public int size() {
        return roadMap.size;
    }

    public ObjectMap.Keys<Position> getPositions() {
        return roadMap.keys();
    }

    public static class Position {
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
            return Objects.hash(row, col);
        }
    }

    public static class AdjacencySet extends ObjectSet<MapCell> {
        /**
         * {@link ObjectSet#ObjectSet()}
         */
        AdjacencySet() {
            super();

        }

        AdjacencySet(int initialCapacity) {
            super(initialCapacity);
        }

        AdjacencySet(int initialCapacity, float loadFactor) {
            super(initialCapacity, loadFactor);
        }

        AdjacencySet(ObjectSet<? extends MapCell> set) {
            super(set);
        }
    }
}
