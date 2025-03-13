package com.tin.game.core;

import com.badlogic.gdx.utils.ObjectSet;
import com.tin.game.utils.Position;

/**
 * Adjacency List Structure implementation for road map.
 * extends {@link com.badlogic.gdx.utils.ObjectMap}
 */
public class RoadMap extends AbstractRoadMap {

    public RoadMap() {
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

        if(adjacency == null) return;

        // clear connections
        adjacency.forEach((adj) -> {
            if(getAdjacent(adj.pos) != null) {
                getAdjacent(adj.pos).remove(cell);
            }
        });

        //delete itself
        remove(adjacency);
    }

    public void excludeConnection(Position start, MapCell cell, Position end) {

        ObjectSet<MapCell> adjacency = getAdjacent(cell.pos);

        // clear connections
        adjacency.forEach((adj) -> {
            if(adj != null && !adj.pos.equals(start) && !adj.pos.equals(end)) {

                getAdjacent(adj.pos).remove(cell);
                if(getAdjacent(adj.pos).size == 0) {
                    remove(getAdjacent(adj.pos));
                }

                getAdjacent(cell.pos).remove(adj);
            }
        });
    }



    @Override
    public void putIfAbsent(MapCell cell, ObjectSet<MapCell> adjacency) {
        if(hasRoad(cell.pos)) return;

        cell.setType(MapCell.CELL_TYPE.ROAD);


        put(cell.pos, adjacency);
    }
}
