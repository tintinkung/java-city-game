package com.tin.game.core;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.tin.game.utils.Position;

public interface IRoadMap {

    void pushRoad(MapCell from, MapCell to);

    void removeRoad(MapCell cell);

    boolean hasRoad(int col, int row);

    boolean hasRoad(Position position);

    ObjectMap.Keys<Position> getPositions();

    ObjectSet<MapCell> getAdjacent(int col, int row);

    ObjectSet<MapCell> getAdjacent(Position position);

    ObjectSet<MapCell> getAdjacent(int col, int row, ObjectSet<MapCell> defaultValue);

    ObjectSet<MapCell> getAdjacent(Position position, ObjectSet<MapCell> defaultValue);
}
