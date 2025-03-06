package com.tin.game;


import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

import static com.tin.game.Config.MAP_WIDTH;
import static com.tin.game.Config.MAP_HEIGHT;
import static com.tin.game.Config.TILE_SIZE;
import static com.tin.game.RoadMap.Position;

public class TownDrawer {

    private final Random random;

    TownDrawer() {
        this.random = new Random();
    }

    public Array<Position> getDebugHouse() {
        Array<Position> debugHouse = new Array<>(true, 2);

        int x1 = random.nextInt(MAP_WIDTH);
        int y1 = random.nextInt(MAP_HEIGHT);

        int x2 = random.nextInt(MAP_WIDTH);
        int y2 = random.nextInt(MAP_HEIGHT);


        while (x1 == x2 && y1 == y2) {
            x2 = random.nextInt(MAP_WIDTH);
            y2 = random.nextInt(MAP_HEIGHT);
        }

        debugHouse.add(new Position(x1, y1));
        debugHouse.add(new Position(x2, y2));

        return debugHouse;
    }

}
