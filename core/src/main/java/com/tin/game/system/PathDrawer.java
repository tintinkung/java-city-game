package com.tin.game.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.tin.game.core.IGameMap;
import space.earlygrey.shapedrawer.JoinType;

public class PathDrawer extends AbstractShapeDrawer {
    public PathDrawer(IGameMap drawMap, Color baseColor) {
        super(drawMap, baseColor);
    }

    public void drawAllPath(ObjectMap.Values<Array<Vector2>> allPath) {
        allPath.forEach((path) -> {
            drawer.path(path, 16.0f, JoinType.SMOOTH, true);
        });

    }
}
