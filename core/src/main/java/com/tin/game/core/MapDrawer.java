package com.tin.game.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.tin.game.utils.Position;

import static com.tin.game.Config.MAP_WIDTH;
import static com.tin.game.Config.MAP_HEIGHT;
import static com.tin.game.Config.TILE_SIZE;

public class MapDrawer extends TiledMapTileLayer implements IMapDrawer {

    public MapDrawer() {
        super(MAP_WIDTH, MAP_HEIGHT, TILE_SIZE, TILE_SIZE);
    }

    /**
     * Creates TiledMap drawer layer with default tile grid
     * populated with {@link MapCell}
     */
    public MapDrawer(boolean populate) {
        super(MAP_WIDTH, MAP_HEIGHT, TILE_SIZE, TILE_SIZE);

        if(populate) initTileGrid(this, false);
    }

    public MapDrawer(boolean populate, boolean debug) {
        super(MAP_WIDTH, MAP_HEIGHT, TILE_SIZE, TILE_SIZE);

        if(populate) initTileGrid(this, debug);
    }

    /**
     * Create a simple black and white checkerboard layer over this tiled map.
     * @see <a href="https://stackoverflow.com/questions/22299785/libgdx-most-efficient-way-to-draw-a-checkerboard-in-background">sources</a>
     * @param opacity The opacity of all tiles in the layer.
     * @return Checkerboard tile map of black and white according to the class' setting.
     * @deprecated
     */
    public static TiledMapTileLayer makeCheckerBoard(float opacity) {

        Pixmap pixmap = new Pixmap(TILE_SIZE * 2, TILE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(255, 255, 255, opacity)); // add your 1 color here
        pixmap.fillRectangle(0, 0, TILE_SIZE, TILE_SIZE);

        pixmap.setColor(new Color(0, 0, 0, opacity)); // add your 2 color here
        pixmap.fillRectangle(TILE_SIZE, 0, TILE_SIZE, TILE_SIZE);

        Texture t = new Texture(pixmap);
        TextureRegion reg1 = new TextureRegion(t, 0, 0, TILE_SIZE, TILE_SIZE);
        TextureRegion reg2 = new TextureRegion(t, TILE_SIZE, 0, TILE_SIZE, TILE_SIZE);

        TiledMapTileLayer layer = new TiledMapTileLayer(MAP_WIDTH, MAP_HEIGHT, TILE_SIZE, TILE_SIZE);

        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                MapDrawer.Cell cell = new MapDrawer.Cell();
                cell.setTile(new StaticTiledMapTile(reg1));
                if (y % 2 != 0) {
                    if (x % 2 != 0) {
                        cell.setTile(new StaticTiledMapTile(reg1));
                    } else {
                        cell.setTile(new StaticTiledMapTile(reg2));
                    }
                } else {
                    if (x % 2 != 0) {
                        cell.setTile(new StaticTiledMapTile(reg2));
                    } else {
                        cell.setTile(new StaticTiledMapTile(reg1));
                    }
                }
                layer.setCell(x, y, cell);
            }
        }

        return layer;
    }

    public static MapDrawer newTileGird(boolean debug) {
        return initTileGrid(new MapDrawer(), debug);
    }

    private static MapDrawer initTileGrid(MapDrawer out, boolean debug) {
        Pixmap pixmap = getTileCellPixmap(debug);

        Texture t = new Texture(pixmap);
        TextureRegion gridCell = new TextureRegion(t, 0, 0, TILE_SIZE, TILE_SIZE);

        for (int row = 0; row < MAP_HEIGHT; row++) {
            for (int col = 0; col < MAP_WIDTH; col++) {
                int x = (col * TILE_SIZE);
                int y = (TILE_SIZE * MAP_HEIGHT) - (row * TILE_SIZE);

                final MapCell cell = new MapCell(x, y, col, row);

                cell.setTile(new StaticTiledMapTile(gridCell));
                out.setCell(col, row, cell);
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

    public MapCell getCellAt(int column, int row) {
        MapCell cell = (MapCell) getCell(column, row);

        if(cell == null) throw new GdxRuntimeException(new IndexOutOfBoundsException("Cell does not exist"));

        return cell;
    }

    @Override
    public MapCell getCellAt(Position position) {
        return getCellAt(position.col, position.row);
    }
}
