package com.tin.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.Objects;
import com.tin.game.MapCell.CELL_BITS;

// ShaperDrawer based road path drawer
public class RoadDrawer implements Disposable {

    public static final float RADIUS = 10.0f;
    public static final float LINE_WIDTH = 4.0f;

    private SpriteBatch drawBatch;
    private ShapeDrawer drawer;
    private Texture drawTexture;
    private MapCell activeCell;
    private Array<MapCell> activeRoad;

    public RoadDrawer() {
        initDrawer();
    }

    private void initDrawer() {
        drawBatch = new SpriteBatch();

        Pixmap roadPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        roadPixmap.setColor(Color.GRAY);
        roadPixmap.drawPixel(0, 0);
        drawTexture = new Texture(roadPixmap); //remember to dispose of later
        roadPixmap.dispose();

        TextureRegion drawRegion = new TextureRegion(drawTexture, 0, 0, 1, 1);
        drawer = new ShapeDrawer(drawBatch, drawRegion);
        drawer.setDefaultLineWidth(LINE_WIDTH);

        activeRoad = new Array<>();
    }

    public void pushRoad(MapCell cell) {
        if(activeCell == null) {
            activeRoad.add(cell);
        }
        else if(!MapCell.checkAdjacency(activeCell, cell)) {
            activeRoad.clear();
            activeRoad.add(cell);
        }
        else {
            activeRoad.add(cell);
        }

        activeCell = cell;
    }

    public void drawActiveRoad() {

        for (int i = 0; i < activeRoad.size; i++) {
            Vector2 center = activeRoad.get(i).getCenter();
            drawer.setColor(Color.GRAY);
            drawer.circle(center.x, center.y, RADIUS);


            drawer.setColor(Color.LIGHT_GRAY);
            drawer.filledCircle(center, RADIUS - 2);

            if(i > 0) {
                MapCell from = activeRoad.get(i - 1);
                MapCell to = activeRoad.get(i);

                float[] vertices = makeAdjacencyVertices(from, to);

                Polygon polygon = new Polygon(vertices);
                polygon.setOrigin(from.getCenter().x, from.getCenter().y);

                // draw connection line
                drawer.setColor(Color.LIGHT_GRAY);
                drawer.filledPolygon(polygon);

                drawer.setColor(Color.GRAY);
                drawer.line(vertices[0], vertices[1], vertices[2], vertices[3]);
                drawer.line(vertices[6], vertices[7], vertices[4], vertices[5]);
            }
        }
    }

    public void begin() {
        drawBatch.begin();
    }

    public void end() {
        drawBatch.end();
    }

    public void dispose() {
        drawTexture.dispose();
    }

    private float[] makeAdjacencyVertices(MapCell from, MapCell to) {
        CELL_BITS adjacency = MapCell.checkAdjacencyType(from, to);
        float[] vertices = new float[8];

        switch (Objects.requireNonNull(adjacency)) {
            // Straight connections
            case TOP:
            case BOTTOM:
                vertices = MapCell.extractVertices(from, to,
                    CELL_BITS.LEFT,
                    CELL_BITS.RIGHT);
                break;
            case LEFT:
            case RIGHT:
                vertices = MapCell.extractVertices(from, to,
                    CELL_BITS.TOP,
                    CELL_BITS.BOTTOM);
                break;
            // Diagonal connections
            case TOP_LEFT:
                vertices = MapCell.extractVertices(from, to,
                    CELL_BITS.TOP_RIGHT,
                    CELL_BITS.BOTTOM_LEFT);
                break;
            case TOP_RIGHT:
                vertices = MapCell.extractVertices(from, to,
                    CELL_BITS.TOP_LEFT,
                    CELL_BITS.BOTTOM_RIGHT);
                break;
            case BOTTOM_LEFT:
                vertices = MapCell.extractVertices(from, to,
                    CELL_BITS.BOTTOM_RIGHT,
                    CELL_BITS.TOP_LEFT);
                break;
            case BOTTOM_RIGHT:
                vertices = MapCell.extractVertices(from, to,
                    CELL_BITS.BOTTOM_LEFT,
                    CELL_BITS.TOP_RIGHT);
                break;
        }
        return vertices;
    }
}
