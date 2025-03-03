package com.tin.game;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntMap;

import static com.tin.game.Config.MAP_HEIGHT;
import static com.tin.game.Config.TILE_SIZE;
import com.tin.game.RoadMap.Position;

public class MapCell extends TiledMapTileLayer.Cell {
    public final int x;
    public final int y;

    private CELL_TYPE type;

    /**
     * The center viewport position of this cell on the board
     */
    private final Vector2 center;

    /**
     * 8 corner reference position from the center of this cell by radius
     */
    private final IntMap<Vector2> corner;

    private final IntMap<Vector2> lhsLane;

    private final IntMap<Vector2> rhsLane;

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
        STORE(3);

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


    MapCell(int screenX, int screenY) {
        super();
        this.x = screenX;
        this.y = screenY;
        this.type = CELL_TYPE.NONE;
        this.center = new Vector2();
        this.corner = new IntMap<>();
        this.lhsLane = new IntMap<>();
        this.rhsLane = new IntMap<>();

        lhsLane.putAll(rhsLane);
        initPositions(RoadDrawer.RADIUS);
    }

    public void initPositions(float radius) {
        float centerX = (float) this.x + (TILE_SIZE / 2.0f);
        float centerY = (float) this.y - (TILE_SIZE / 2.0f);
        this.center.set(centerX, centerY);

        float cos45 = radius * MathUtils.cosDeg(45.0f);
        float sin45 = radius * MathUtils.sinDeg(45.0f);
        float cos30 = radius * MathUtils.cosDeg(30.0f);
        float sin30 = radius * MathUtils.sinDeg(30.0f);

        float cos15 = radius * MathUtils.cosDeg(15.0f);
        float sin15 = radius * MathUtils.cosDeg(75.0f);

        corner.put(CELL_BITS.TOP.id(), new Vector2(centerX, centerY + radius));
        corner.put(CELL_BITS.LEFT.id(), new Vector2(centerX - radius, centerY));
        corner.put(CELL_BITS.RIGHT.id(), new Vector2(centerX + radius, centerY));
        corner.put(CELL_BITS.BOTTOM.id(), new Vector2(centerX, centerY - radius));
        corner.put(CELL_BITS.TOP_LEFT.id(), new Vector2(centerX - cos45, centerY + sin45));
        corner.put(CELL_BITS.TOP_RIGHT.id(), new Vector2(centerX + cos45, centerY + sin45));
        corner.put(CELL_BITS.BOTTOM_LEFT.id(), new Vector2(centerX - cos45, centerY - sin45));
        corner.put(CELL_BITS.BOTTOM_RIGHT.id(), new Vector2(centerX + cos45, centerY - sin45));

        lhsLane.put(CELL_BITS.TOP.id(), new Vector2(centerX + sin30, centerY + cos30));
        lhsLane.put(CELL_BITS.LEFT.id(), new Vector2(centerX - cos30, centerY - sin30));
        lhsLane.put(CELL_BITS.RIGHT.id(), new Vector2(centerX + cos30, centerY + sin30));
        lhsLane.put(CELL_BITS.BOTTOM.id(), new Vector2(centerX - sin30, centerY - cos30));
        lhsLane.put(CELL_BITS.TOP_LEFT.id(), new Vector2(centerX - sin15, centerY + cos15));
        lhsLane.put(CELL_BITS.TOP_RIGHT.id(), new Vector2(centerX + sin15, centerY + cos15));
        lhsLane.put(CELL_BITS.BOTTOM_LEFT.id(), new Vector2(centerX - sin15, centerY - cos15));
        lhsLane.put(CELL_BITS.BOTTOM_RIGHT.id(), new Vector2(centerX + sin15, centerY - cos15));

        rhsLane.put(CELL_BITS.TOP.id(), new Vector2(centerX - sin30, centerY + cos30));
        rhsLane.put(CELL_BITS.LEFT.id(), new Vector2(centerX - cos30, centerY + sin30));
        rhsLane.put(CELL_BITS.RIGHT.id(), new Vector2(centerX + cos30, centerY - sin30));
        rhsLane.put(CELL_BITS.BOTTOM.id(), new Vector2(centerX + sin30, centerY - cos30));
        rhsLane.put(CELL_BITS.TOP_LEFT.id(), new Vector2(centerX - cos15, centerY + sin15));
        rhsLane.put(CELL_BITS.TOP_RIGHT.id(), new Vector2( centerX + cos15, centerY + sin15));
        rhsLane.put(CELL_BITS.BOTTOM_LEFT.id(), new Vector2(centerX - cos15, centerY - sin15));
        rhsLane.put(CELL_BITS.BOTTOM_RIGHT.id(), new Vector2(centerX + cos15, centerY - sin15));
    }

    public Vector2 getCenter() {
        return this.center;
    }

    public Vector2 getCorner(CELL_BITS cornerBits) {
        return corner.get(cornerBits.id());
    }

    /**
     * Get a lane's key coordinate to determine path drawing
     * @param cornerBits The Cell bits to get lane from
     * @param lane Lane type to get, left or right lane
     * @return The lane coordinate by cell bits
     */
    public Vector2 getLane(CELL_BITS cornerBits, LANE lane) {
        return this.getLane(cornerBits, lane.id());
    }

    /**
     * Get a lane's key coordinate to determine path drawing
     * @param cornerBits The Cell bits to get lane from
     * @param forwardLane Whether to get the forward lane (left hand side) or the opposite lane
     * @return The lane coordinate by cell bits (returns left lane is forwardLane is true)
     */
    public Vector2 getLane(CELL_BITS cornerBits, boolean forwardLane) {
        if(forwardLane) return lhsLane.get(cornerBits.id());
        else return rhsLane.get(cornerBits.id());
    }

    public CELL_TYPE getType() {
        return  this.type;
    }

    /**
     * Get the Road Map {@link RoadMap.Position} of this cell.
     * @return Position by row and column
     */
    public Position getPosition() {
        return remapScreenToCell(this.x, this.y);
    }

    public static Position remapScreenToCell(int screenX, int screenY) {
        return new Position(
            Math.floorDiv(screenX, TILE_SIZE),
            Math.floorDiv(TILE_SIZE * MAP_HEIGHT - screenY, TILE_SIZE)
        );
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
}
