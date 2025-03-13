package com.tin.game.system;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.tin.game.core.AbstractDrawer;
import com.tin.game.core.IGameMap;
import space.earlygrey.shapedrawer.ShapeDrawer;

public abstract class AbstractShapeDrawer extends AbstractDrawer {
    protected SpriteBatch drawBatch;
    protected ShapeDrawer drawer;
    protected Texture drawTexture;

    public AbstractShapeDrawer(IGameMap drawMap, Color baseColor) {
        super(drawMap);
        initDrawer(baseColor);
    }

    public void begin() {
        drawBatch.begin();
    }

    public void end() {
        drawBatch.end();
    }

    public void dispose() {
        drawTexture.dispose();
        drawBatch.dispose();
    }

    protected void initDrawer(Color baseColor) {
        drawBatch = new SpriteBatch();

        Pixmap drawPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

        // Main tint, this brightens all the road
        drawPixmap.setColor(baseColor);
        drawPixmap.drawPixel(0, 0);
        drawTexture = new Texture(drawPixmap); //remember to dispose of later
        drawPixmap.dispose();

        TextureRegion drawRegion = new TextureRegion(drawTexture, 0, 0, 1, 1);
        drawer = new ShapeDrawer(drawBatch, drawRegion);
    }
}
