package com.tin.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;

import static com.tin.game.Config.MAP_WIDTH;
import static com.tin.game.Config.MAP_HEIGHT;
import static com.tin.game.Config.TILE_SIZE;

public class TiledMapDebug {

    /**
     * Create a simple black and white checkerboard layer over this tiled map.
     * @see <a href="https://stackoverflow.com/questions/22299785/libgdx-most-efficient-way-to-draw-a-checkerboard-in-background">sources</a>
     * @param opacity The opacity of all tiles in the layer.
     * @return Checkerboard tile map of black and white according to the class' setting.
     */
    public TiledMapTileLayer getCheckerBoard(float opacity) {

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
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
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


}
