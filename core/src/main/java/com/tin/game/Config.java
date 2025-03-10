package com.tin.game;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

/**
 * Global configuration references values to use throughout this code.
 */
public class Config {
    public static final String TITLE = "Tin's City Game";

    // Map and Tile settings
    public static final int DEFAULT_WIDTH = 640;
    public static final int DEFAULT_HEIGHT = 480;

    // Game Grid tile/cell size in pixel
    public static final int TILE_SIZE = 32;

    // default dimension for tile width 32 * 20 = 640
    public static final int MAP_WIDTH = 20;

    // default dimension for tile width 32 * 15 = 480
    public static final int MAP_HEIGHT = 15;

    public static final int STEP = 1/60;

    public static class GameStates {
        public static final int LOADING = 10000;
        public static final int MENU = 10001;
    }

    public static TileSetJSON getTileSetConfig(FileHandle config) {
        final Json json = new Json();
        return json.fromJson(TileSetJSON.class, config);
    }

    /**
     * Utility class for de-serializing tile-set configuration from file
     * Edit this according to tileset.json resource file.
     */
    public static class TileSetJSON {
        private String texturePath;
        private int tileWidth;
        private int tileHeight;
        private Array<Array<String>> terrainDefs;

        public void setTexturePath(String texturePath) {
            this.texturePath = texturePath;
        }

        public void setTileWidth(int tileWidth) {
            this.tileWidth = tileWidth;
        }

        public void setTileHeight(int tileHeight) {
            this.tileHeight = tileHeight;
        }

        public void setTerrainDefs(Array<Array<String>> terrainDefs) {
            this.terrainDefs = terrainDefs;
        }

        public String getTexturePath() {
            return texturePath;
        }

        public int getTileWidth() {
            return tileWidth;
        }

        public int getTileHeight() {
            return tileHeight;
        }

        public Array<Array<String>> getTerrainDefs() {
            return terrainDefs;
        }
    }

}
