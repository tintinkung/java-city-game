package com.tin.game.core;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.StringBuilder;
import com.tin.game.utils.Position;

public abstract class AbstractRoadMap extends ObjectMap<Position, ObjectSet<MapCell>> implements IRoadMap {

    AbstractRoadMap() {
        super();
    }

    public AbstractRoadMap(int initialCapacity) {
        super(initialCapacity);
    }

    public AbstractRoadMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public AbstractRoadMap(ObjectMap<? extends Position, ? extends ObjectSet<MapCell>> map) {
        super(map);
    }

    public abstract void pushRoad(MapCell from, MapCell to);

    public abstract void removeRoad(MapCell cell);

    public void put(int col, int row, ObjectSet<MapCell> adjacency) {
        put(new Position(col, row), adjacency);
    }

    public void putIfAbsent(Position position, ObjectSet<MapCell> adjacency) {
        if(hasRoad(position)) return;
        put(position, adjacency);
    }

    public void putIfAbsent(int col, int row, ObjectSet<MapCell> adjacency) {
        if(hasRoad(col, row)) return;
        put(col, row, adjacency);
    }

    public void putIfAbsent(MapCell cell, ObjectSet<MapCell> adjacency) {
        if(hasRoad(cell.pos)) return;
        put(cell.pos, adjacency);
    }

    public void remove(int col, int row) {
        remove(new Position(col, row));
    }

    public void remove(ObjectSet<MapCell> adjacency) {
        remove(findKey(adjacency, false));
    }

    @Override
    public ObjectSet<MapCell> getAdjacent(int col, int row) {
        return get(new Position(col, row));
    }

    @Override
    public ObjectSet<MapCell> getAdjacent(Position position) {
        return get(position);
    }

    @Override
    public ObjectSet<MapCell> getAdjacent(int col, int row, ObjectSet<MapCell> defaultValue) {
        return get(new Position(col, row), defaultValue);
    }

    @Override
    public ObjectSet<MapCell> getAdjacent(Position position, ObjectSet<MapCell> defaultValue) {
        return get(position, defaultValue);
    }

    @Override
    public boolean hasRoad(int col, int row) {
        return containsKey(new Position(col, row));
    }

    @Override
    public boolean hasRoad(Position position) {
        return containsKey(position);
    }

    @Override
    public ObjectMap.Keys<Position> getPositions() {
        return keys();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        this.forEach((node) -> {
            result.append(node.key.toString());
            result.append(": {");
            node.value.forEach((edges) -> {
                if(result.charAt(result.length() - 1) == ')')
                    result.append(", ");

                result.append("(")
                    .append(edges.pos.col).append(", ")
                    .append(edges.pos.row).append(")");
            });
            result.append("}\n");
        });

        return result.toString();
    }

}
