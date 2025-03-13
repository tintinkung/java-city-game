package com.tin.game.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.tin.game.utils.Position;

import java.util.Arrays;

import static com.tin.game.Config.*;

public class GameMap extends TiledMapTileLayer implements IGameMap {

    private Array<Position> allCell;

    public GameMap() {
        super(MAP_WIDTH, MAP_HEIGHT, TILE_SIZE, TILE_SIZE);
        this.allCell = new Array<>();
    }

    /**
     * Creates TiledMap drawer layer with default tile grid
     * populated with {@link MapCell}
     */
    public GameMap(boolean populate) {
        super(MAP_WIDTH, MAP_HEIGHT, TILE_SIZE, TILE_SIZE);

        if(populate) initTileGrid(this, false);
    }

    public GameMap(boolean populate, boolean debug) {
        super(MAP_WIDTH, MAP_HEIGHT, TILE_SIZE, TILE_SIZE);

        if(populate) initTileGrid(this, debug);
    }

    public static GameMap newBlankMap(int width, int height, Color color) {
        return getBlankMap(width, height, color);
    }

    public static GameMap newBlankMap(int width, int height) {
        return getBlankMap(width, height, new Color(0x2f2f2fff));
    }

    public static GameMap newTileGird(boolean debug) {
        return initTileGrid(new GameMap(), debug);
    }

    private static GameMap initTileGrid(GameMap out, boolean debug) {
        Pixmap pixmap = getTileCellPixmap(debug);
        out.allCell = new Array<>();

        Texture t = new Texture(pixmap);
        TextureRegion gridCell = new TextureRegion(t, 0, 0, TILE_SIZE, TILE_SIZE);

        for (int row = 0; row < MAP_HEIGHT; row++) {
            for (int col = 0; col < MAP_WIDTH; col++) {
                int x = (col * TILE_SIZE);
                int y = (TILE_SIZE * MAP_HEIGHT) - (row * TILE_SIZE);

                final MapCell cell = new MapCell(x, y, col, row);

                cell.setTile(new StaticTiledMapTile(gridCell));
                out.setCell(col, row, cell);
                out.allCell.add(new Position(col, row));

            }
        }

        return out;
    }

    private static Pixmap getTileCellPixmap(Pixmap pixmap) {
        pixmap.setColor(new Color(0x3d3d3dff));
        pixmap.drawLine(0, 0, TILE_SIZE - 1, 0);
        pixmap.drawLine(TILE_SIZE - 1, 0, TILE_SIZE - 1, TILE_SIZE - 1);
        pixmap.drawLine(TILE_SIZE - 1, TILE_SIZE - 1, 0, TILE_SIZE - 1);
        pixmap.drawLine(0, TILE_SIZE, 0, 0);

        pixmap.setColor(new Color(0x2f2f2fff));
        pixmap.fillRectangle(1, 1, TILE_SIZE - 2, TILE_SIZE - 2);

        return pixmap;
    }

    private static Pixmap getTileCellPixmap(boolean debug) {
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);

        if(!debug) return getTileCellPixmap(pixmap);

        // top left -> top right
        pixmap.setColor(Color.RED);
        pixmap.drawLine(0, 0, TILE_SIZE - 1, 0);

        // top right -> bottom right
        pixmap.setColor(Color.ORANGE);
        pixmap.drawLine(TILE_SIZE - 1, 0, TILE_SIZE - 1, TILE_SIZE - 1);

        // bottom right -> bottom left
        pixmap.setColor(Color.CYAN);
        pixmap.drawLine(TILE_SIZE - 1, TILE_SIZE - 1, 0, TILE_SIZE - 1);

        // bottom left -> top left
        pixmap.setColor(Color.GREEN);
        pixmap.drawLine(0, TILE_SIZE, 0, 0);

        // cell color
        pixmap.setColor(Color.BLACK);
        pixmap.fillRectangle(1, 1, TILE_SIZE - 2, TILE_SIZE - 2);

        return pixmap;
    }

    private static GameMap getBlankMap(int width, int height, Color fillColor) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        pixmap.setColor(fillColor);
        pixmap.fillRectangle(0, 0, width, height);

        Texture fillTexture = new Texture(pixmap);
        TextureRegion gridCell = new TextureRegion(fillTexture, 0, 0, width, height);
        final MapCell cell = new MapCell(0, 0, 0, 0);

        cell.setTile(new StaticTiledMapTile(gridCell));

        GameMap map = new GameMap();
        map.setCell(0, 0, cell);
        map.allCell.add(new Position(0, 0));
        return map;
    }

    public Array<Position> getAllCell() {
        return allCell;
    }

    public Array<Position> getEmptyCells() {
        Array<Position> emptyCell = new Array<>();
        allCell.forEach((cell) -> {
            if(getCellAt(cell.col, cell.row).getType() == MapCell.CELL_TYPE.NONE) {
                emptyCell.add(cell);
            }
        });

        return emptyCell;
    }

    public Array<Position> getEmptyQuadCells() {
        Array<Position> emptyCell = new Array<>();
        allCell.forEach((cell) -> {
            if(cell.col >= this.getWidth() - 2) return;
            if(cell.row >= this.getHeight() - 2) return;

            MapCell thisCell = getCellAt(cell.col, cell.row);
            MapCell xCell = getCellAt(cell.col + 1, cell.row);
            MapCell yCell = getCellAt(cell.col, cell.row + 1);
            MapCell xyCell = getCellAt(cell.col + 1, cell.row + 1);

            if(thisCell.getType()   == MapCell.CELL_TYPE.NONE
                && xCell.getType()  == MapCell.CELL_TYPE.NONE
                && xyCell.getType() == MapCell.CELL_TYPE.NONE
                && yCell.getType()  == MapCell.CELL_TYPE.NONE) {
                emptyCell.add(cell);
            }
        });

        return emptyCell;
    }

    public Array<Position> getAdjacentCell(Position cell, MapCell.CELL_TYPE... include) {

        Array<Position> adjacent = new Array<>();
        Array<Position> bias = new Array<>(new Position[] {
            new Position( 0, -1), // Up
            new Position( 0,  1), // Down
            new Position(-1,  0), // Left
            new Position( 1,  0), // Right
            new Position(-1, -1), // Top-left
            new Position( 1, -1), // Top-right
            new Position(-1,  1), // Bottom-left
            new Position( 1,  1), // Bottom-right
        });

        for(Position dir : bias) {
            int newCol = cell.col + dir.col;
            int newRow = cell.row + dir.row;

            if(newCol < 0 || newCol >= MAP_WIDTH) continue;
            if(newRow < 0 || newRow >= MAP_HEIGHT) continue;

            MapCell newCell = getCellAt(newCol, newRow);
            boolean passed = Arrays
                .stream(include)
                .anyMatch(c -> c.id() == newCell.getType().id());
            if(!passed) continue;

            adjacent.add(new Position(newCol, newRow));
        }

        return adjacent;
    }

    public MapCell getCellAt(int column, int row) {
        MapCell cell = (MapCell) getCell(column, row);

        if(cell == null) throw new GdxRuntimeException(new IndexOutOfBoundsException("Cell (" + column + ", " + row + ") does not exist"));

        return cell;
    }

    @Override
    public MapCell getCellAt(Position position) {
        return getCellAt(position.col, position.row);
    }
}
