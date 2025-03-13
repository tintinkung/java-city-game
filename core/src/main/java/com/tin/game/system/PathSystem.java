package com.tin.game.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.BSpline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedSet;
import com.tin.game.core.AbstractDrawer;
import com.tin.game.core.IGameMap;
import com.tin.game.core.MapCell;
import com.tin.game.core.PathMap;
import com.tin.game.utils.Position;

public class PathSystem extends AbstractDrawer {

    private final ObjectMap<Color, BSpline<Vector2>> splineMap;
    private final ObjectMap<Color, Array<Vector2>> pathMap;
    private final ObjectMap<Color, Array<Position>> cellMap;

    private final OnPushPath onPushPath;

    @FunctionalInterface
    public interface OnPushPath {
        void excludeConnection(Position start, MapCell cell, Position end);
    }

    public PathSystem(IGameMap drawMap, OnPushPath onPushPath) {
        super(drawMap);
        this.splineMap = new ObjectMap<>();
        this.pathMap = new ObjectMap<>();
        this.cellMap = new ObjectMap<>();
        this.onPushPath = onPushPath;
    }

    private void markPath(OrderedSet<Position> path) {
        path.forEach((position) -> {
            MapCell cell = drawMap.getCellAt(position);
            cell.setType(MapCell.CELL_TYPE.HIGHWAY);
        });
    }

    private void clearPathConnection(OrderedSet<Position> path) {
        Array<Position> cellPath = path.orderedItems();
        for (int i = 1; i < cellPath.size - 1; i++) {
            Position start = cellPath.get(i - 1);
            MapCell cell = drawMap.getCellAt(cellPath.get(i));
            Position end = cellPath.get(i + 1);
            this.onPushPath.excludeConnection(start, cell, end);
        }
    }

    public void pushPath(Color colorID, Array<PathMap.SubPath> pathFind) {
        OrderedSet<Vector2> dataset = new OrderedSet<>();
        OrderedSet<Position> cellData = new OrderedSet<>();
        Gdx.app.log("path", "got path finding");

        pathFind.forEach((path) -> {
            Gdx.app.log("path", path.getStart() + " -> " + path.getEnd() + " " + path.debugColor);

            // Check and reverse if path is not flipped
            Array<Position> latest = cellData.orderedItems();
            Array<Position> cell = path.getCellVertices().orderedItems();
            Array<Vector2> draw = path.getDrawVertices().orderedItems();

            if(latest.size >= 1 && !latest.get(latest.size - 1).equals(path.getStart())) {
                cell.reverse();
                draw.reverse();
            }

            dataset.addAll(draw);
            cellData.addAll(cell);
        });

        markPath(cellData);
        clearPathConnection(cellData);

        Array<Vector2> ordered = dataset.orderedItems();
        Vector2[] items = new Vector2[dataset.size + 2];
        items[0] = ordered.get(0);
        for (int i = 0; i < ordered.size; i++) items[i + 1] = ordered.get(i);
        items[dataset.size + 1] = ordered.get(ordered.size - 1);

        this.splineMap.put(colorID, new BSpline<>(items, 3, false));
        this.pathMap.put(colorID, ordered);
        this.cellMap.put(colorID, cellData.orderedItems());
    }

    public BSpline<Vector2> getSpline(Color colorID) {
        return this.splineMap.get(colorID);
    }

    public Array<Position> getCellMap(Color colorID) {
        return this.cellMap.get(colorID);
    }

    public ObjectMap.Values<Array<Vector2>> getAllPath() {
        return this.pathMap.values();
    }

    public void removePath(Color colorID) {
        this.pathMap.remove(colorID);
    }
}
