package com.tin.game.core;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntMap;
import com.tin.game.system.RoadDrawer;
import com.tin.game.utils.Position;

import static com.tin.game.Config.*;
import static com.tin.game.Config.MAP_WIDTH;

public class MapCell extends GameMap.Cell {

    public final int x;
    public final int y;

    public final Position pos;

    private CELL_TYPE type;

    /**
     * The center viewport position of this cell on the board
     */
    private final Vector2 center;

    /**
     * 8 corner reference position from the center of this cell by radius
     */
    private final IntMap<Vector2> corner;

    public enum LANE {
        LEFT(true), // lhs, the forward lane
        RIGHT(false);

        private final boolean value;

        LANE(boolean value) {
            this.value = value;
        }

        public boolean id() {
            return value;
        }
    }

    public enum CELL_TYPE {
        NONE(0),
        ROAD(1),
        HOUSE(2),
        STORE(3),
        HIGHWAY(4);

        private final int value;

        CELL_TYPE(int value) {
            this.value = value;
        }

        public int id() {
            return value;
        }
    }

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

        public CELL_BITS opposite() {
            switch (this) {
                case TOP: return BOTTOM;
                case BOTTOM: return TOP;
                case LEFT: return RIGHT;
                case RIGHT: return LEFT;
                case TOP_LEFT: return BOTTOM_RIGHT;
                case TOP_RIGHT: return BOTTOM_LEFT;
                case BOTTOM_LEFT: return TOP_RIGHT;
                case BOTTOM_RIGHT: return TOP_LEFT;
            }
            return this;
        }
    }


    public MapCell(int screenX, int screenY, int col, int row) {
        super();
        this.x = screenX + OFFSET_X;
        this.y = screenY + OFFSET_Y;
        this.pos = new Position(col, row);
        this.type = CELL_TYPE.NONE;
        this.center = new Vector2();
        this.corner = new IntMap<>();

        initPositions(RoadDrawer.RADIUS);
    }

    public void initPositions(float radius) {
        float centerX = (float) this.x + (TILE_SIZE / 2.0f);
        float centerY = (float) this.y - (TILE_SIZE / 2.0f);
        this.center.set(centerX, centerY);

        float cos45 = radius * MathUtils.cosDeg(45.0f);
        float sin45 = radius * MathUtils.sinDeg(45.0f);

        corner.put(CELL_BITS.TOP.id(), new Vector2(centerX, centerY + radius));
        corner.put(CELL_BITS.LEFT.id(), new Vector2(centerX - radius, centerY));
        corner.put(CELL_BITS.RIGHT.id(), new Vector2(centerX + radius, centerY));
        corner.put(CELL_BITS.BOTTOM.id(), new Vector2(centerX, centerY - radius));
        corner.put(CELL_BITS.TOP_LEFT.id(), new Vector2(centerX - cos45, centerY + sin45));
        corner.put(CELL_BITS.TOP_RIGHT.id(), new Vector2(centerX + cos45, centerY + sin45));
        corner.put(CELL_BITS.BOTTOM_LEFT.id(), new Vector2(centerX - cos45, centerY - sin45));
        corner.put(CELL_BITS.BOTTOM_RIGHT.id(), new Vector2(centerX + cos45, centerY - sin45));
    }

    public Vector2 getCenter() {
        return this.center.cpy();
    }

    public Vector2 getCorner(CELL_BITS cornerBits) {
        return corner.get(cornerBits.id());
    }

    public Vector2 getCorner(int cornerBits) {
        return corner.get(cornerBits);
    }

    public CELL_TYPE getType() {
        return  this.type;
    }

    /**@deprecated  */
    public static Position positionFromCoordinate(int screenX, int screenY) {
        return new Position(
            Math.floorDiv(screenX, TILE_SIZE),
            Math.floorDiv(TILE_SIZE * MAP_HEIGHT - screenY, TILE_SIZE)
        );
    }

    public static Position remapScreenToCell(int screenX, int screenY) {
        return remapScreenToCell((float) screenX, (float) screenY);
    }

    public static Position remapScreenToCell(Vector2 coordinate) {
        return remapScreenToCell(coordinate.x, coordinate.y);
    }


    /**
     * Remap viewport coordinate into what cell position it is in.
     * @param screenX viewport X position anywhere in between a cell.
     * @param screenY viewport Y position anywhere in between a cell.
     * @return The cell position if found, null if not.
     */
    public static Position remapScreenToCell(float screenX, float screenY) {
        int column =  MathUtils.floor(
            MathUtils.map(OFFSET_X,
                MAP_WIDTH * TILE_SIZE + OFFSET_X,
                0.0f, MAP_WIDTH, screenX)
        );

        if(column < 0 || column >= MAP_WIDTH) return null;

        int row = MathUtils.floor(
            MathUtils.map(OFFSET_Y,
                MAP_HEIGHT * TILE_SIZE + OFFSET_Y,
                MAP_HEIGHT, 0.0f, screenY)
        );

        if(row < 0 || row >= MAP_HEIGHT) return null;

        return new Position(column, row);
    }

    public void setType(CELL_TYPE type) {
        this.type = type;
    }

    public boolean isOccupied() {
        return type.id() != CELL_TYPE.NONE.id();
    }

    public void clearCell() {
        this.type = CELL_TYPE.NONE;
    }

    public static CELL_BITS checkAdjacencyType(MapCell from, MapCell to) {
        if(to.x - from.x == TILE_SIZE) { // right adjacency
            // up adjacency
            if(to.y - from.y == TILE_SIZE) return CELL_BITS.TOP_RIGHT;
            else if(from.y - to.y == TILE_SIZE) return CELL_BITS.BOTTOM_RIGHT;
            else return CELL_BITS.RIGHT;
        }
        else if(from.x - to.x == TILE_SIZE) { // left adjacency
            // up adjacency
            if(to.y - from.y == TILE_SIZE) return CELL_BITS.TOP_LEFT;
            else if(from.y - to.y == TILE_SIZE) return CELL_BITS.BOTTOM_LEFT;
            else return CELL_BITS.LEFT;
        }
        else { // no x adjacency, must be y
            // up adjacency
            if(to.y - from.y == TILE_SIZE) return CELL_BITS.TOP;
            else if(from.y - to.y == TILE_SIZE) return CELL_BITS.BOTTOM;
        }

        return null;
    }

    public static float[] extractVertices(
        MapCell from,
        MapCell to,
        CELL_BITS cornerFrom,
        CELL_BITS cornerTo) {
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

    public static boolean isAdjacent(MapCell from, MapCell to) {
        return Math.abs(to.x - from.x) <= TILE_SIZE && Math.abs(to.y - from.y) <= TILE_SIZE;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MapCell other = (MapCell) obj;
        return this.pos == other.pos;
    }
}
