package com.tin.game.core;

import com.badlogic.gdx.utils.ObjectSet;

/**
 * Adjacency List Structure implementation for road map.
 * extends {@link com.badlogic.gdx.utils.ObjectMap}
 */
public class RoadMap extends AbstractRoadMap {

    RoadMap() {
        super();
    }

    public void pushRoad(MapCell from, MapCell to) {
        // put key node with blank adjacency
        putIfAbsent(from, new ObjectSet<>());
        putIfAbsent(to, new ObjectSet<>());
        if(from.equals(to)) return;

        // add adjacency to node
        getAdjacent(from.pos).add(to);
        getAdjacent(to.pos).add(from);
    }

    public void removeRoad(MapCell cell) {
        ObjectSet<MapCell> adjacency = getAdjacent(cell.pos);

        // clear connections
        adjacency.forEach((adj) -> {
            getAdjacent(adj.pos).remove(cell);
        });

        //delete itself
        remove(adjacency);
    }

    @Override
    public void putIfAbsent(MapCell cell, ObjectSet<MapCell> adjacency) {
        if(hasRoad(cell.pos)) return;

        cell.setType(MapCell.CELL_TYPE.ROAD);

        put(cell.pos, adjacency);
    }
}
