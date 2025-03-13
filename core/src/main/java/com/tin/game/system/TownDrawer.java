package com.tin.game.system;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.tin.game.Config;
import com.tin.game.core.IGameMap;
import com.tin.game.core.MapCell;

/// TODO: move house generation to here

public class TownDrawer extends AbstractShapeDrawer {

    public static final int INSET_PIXEL = 4;

    public TownDrawer(IGameMap drawMap, Color baseColor) {
        super(drawMap, baseColor);
    }

    public void drawAllHouse(ObjectMap<Color, Array<House>> houseMap) {
        houseMap.forEach((entry -> {
            Color colorID = entry.key;
            entry.value.forEach((house) -> {
                MapCell cell = drawMap.getCellAt(house);
                drawer.filledRectangle(
                    cell.x + INSET_PIXEL,
                    cell.y - INSET_PIXEL,
                    Config.TILE_SIZE - (INSET_PIXEL * 2),
                    (Config.TILE_SIZE * -1) + (INSET_PIXEL * 2),
                    colorID);
            });
        }));
    }

    public void drawAllStore(ObjectMap<Color, Store> storeMap) {
        storeMap.forEach((entry -> {
            Store store = entry.value;
            Color colorID = entry.key;
            MapCell cell = drawMap.getCellAt(store);

            drawer.filledRectangle(
                cell.x + INSET_PIXEL,
                cell.y - INSET_PIXEL,
                (Config.TILE_SIZE * 2) - (INSET_PIXEL * 2),
                (Config.TILE_SIZE * -2) + (INSET_PIXEL * 2),
                colorID);
        }));
    }

}
