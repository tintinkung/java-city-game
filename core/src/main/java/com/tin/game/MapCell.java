package com.tin.game;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;

public class MapCell extends TiledMapTileLayer.Cell {
    public final int x;
    public final int y;
    private int tileSize;
    private Vector2 center;
    private HashMap<Integer, Vector2> corner;

    public enum CELL_BITS {
        TOP(0),
        LEFT(1),
        RIGHT(2),
        BOTTOM(3),
        TOP_LEFT(4),
        TOP_RIGHT(5),
        BOTTOM_LEFT(6),
        BOTTOM_RIGHT(7);

        private final int value;

        CELL_BITS(int value) {
            this.value = value;
        }

        public int id() {
            return value;
        }
    }


    MapCell(int screenX, int screenY, int tileSize) {
        super();
        this.tileSize = tileSize;
        this.x = screenX;
        this.y = screenY;
        this.center = new Vector2();
        this.corner = new HashMap<>();
        initPositions();
    }

    public void initPositions() {
        float centerX = (float) this.x + (tileSize / 2.0f);
        float centerY = (float) this.y - (tileSize / 2.0f);
        this.center.set(centerX, centerY);

        corner.put(CELL_BITS.TOP.id(), new Vector2(centerX, centerY + RoadDrawer.RADIUS));
        corner.put(CELL_BITS.LEFT.id(), new Vector2(centerX - RoadDrawer.RADIUS, centerY));
        corner.put(CELL_BITS.RIGHT.id(), new Vector2(centerX + RoadDrawer.RADIUS, centerY));
        corner.put(CELL_BITS.BOTTOM.id(), new Vector2(centerX, centerY - RoadDrawer.RADIUS));
        corner.put(CELL_BITS.TOP_LEFT.id(), corner.get(CELL_BITS.TOP.id()).cpy().rotateAroundDeg(this.center, 45.0f));
        corner.put(CELL_BITS.TOP_RIGHT.id(), corner.get(CELL_BITS.RIGHT.id()).cpy().rotateAroundDeg(this.center, 45.0f));
        corner.put(CELL_BITS.BOTTOM_LEFT.id(), corner.get(CELL_BITS.LEFT.id()).cpy().rotateAroundDeg(this.center, 45.0f));
        corner.put(CELL_BITS.BOTTOM_RIGHT.id(), corner.get(CELL_BITS.BOTTOM.id()).cpy().rotateAroundDeg(this.center, 45.0f));
    }

    public Vector2 getCenter() {
        return this.center;
    }

    public Vector2 getCorner(CELL_BITS cornerBits) {
        return corner.get(cornerBits.id());
    }

    public int getTileSize() {
        return tileSize;
    }

    public static CELL_BITS checkAdjacencyType(MapCell from, MapCell to) {
        int unit = to.getTileSize();

        if(to.x - from.x == unit) { // right adjacency
            if(to.y - from.y == unit) // up adjacency
                return CELL_BITS.TOP_RIGHT;
            else if(from.y - to.y == unit)
                return CELL_BITS.BOTTOM_RIGHT;
            else return CELL_BITS.RIGHT;
        }
        else if(from.x - to.x == unit) { // left adjacency
            if(to.y - from.y == unit) // up adjacency
                return CELL_BITS.TOP_LEFT;
            else if(from.y - to.y == unit)
                return CELL_BITS.BOTTOM_LEFT;
            else return CELL_BITS.LEFT;
        }
        else { // no x adjacency, must be y
            if(to.y - from.y == unit) // up adjacency
                return CELL_BITS.TOP;
            else if(from.y - to.y == unit)
                return CELL_BITS.BOTTOM;
        }

        return null;
    }

    public static float[] extractVertices(
        MapCell from,
        MapCell to,
        MapCell.CELL_BITS cornerFrom,
        MapCell.CELL_BITS cornerTo) {
        Vector2 xFrom = from.getCorner(cornerFrom);
        Vector2 xTo = to.getCorner(cornerFrom);
        Vector2 yTo = to.getCorner(cornerTo);
        Vector2 yFrom = from.getCorner(cornerTo);

        return new float[] {
            xFrom.x, xFrom.y,
            xTo.x, xTo.y,
            yTo.x, yTo.y,
            yFrom.x, yFrom.y,
        };
    }

    public static boolean checkAdjacency(MapCell from, MapCell to) {
        return Math.abs(to.x - from.x) <= to.getTileSize() && Math.abs(to.y - from.y) <= to.getTileSize();
    }
}
