package com.tin.game.system;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.tin.game.core.*;
import com.tin.game.utils.Position;

public class RoadDrawer extends AbstractShapeDrawer {

    public static final float RADIUS = 10.0f;
    public static final float LINE_WIDTH = 4.0f;

    public RoadDrawer(IGameMap drawMap, Color baseColor) {
        super(drawMap, baseColor);
        drawer.setDefaultLineWidth(LINE_WIDTH);
    }

    public void drawEachRoadCell(RoadMap roadMap) {
        roadMap.getPositions().forEach((node) -> {
            Vector2 center = drawMap.getCellAt(node).getCenter();
            drawer.setColor(Color.LIGHT_GRAY);
            drawer.circle(center.x, center.y, RADIUS);
            drawer.setColor(Color.GRAY);
            drawer.filledCircle(center, RADIUS - 2);
        });
    }

    public void drawEachRoadConnection(PathMap pathMap) {
        pathMap.getVisitedEdges().forEach((edge) -> {
            Position fromPos = edge.pos1;
            Position toPos = edge.pos2;

            MapCell from = drawMap.getCellAt(fromPos);
            MapCell to = drawMap.getCellAt(toPos);

            float[] vertices = makeAdjacencyVertices(from, to);

            if(vertices == null) return;

            Polygon polygon = new Polygon(vertices);
            polygon.setOrigin(from.getCenter().x, from.getCenter().y);

            // draw connection line
            drawer.setColor(Color.GRAY);
            drawer.filledPolygon(polygon);
            drawer.setColor(Color.LIGHT_GRAY);
            drawer.line(vertices[0], vertices[1], vertices[2], vertices[3]);
            drawer.line(vertices[6], vertices[7], vertices[4], vertices[5]);
        });
    }

    private float[] makeAdjacencyVertices(MapCell from, MapCell to) {
        MapCell.CELL_BITS adjacency = MapCell.checkAdjacencyType(from, to);
        if(adjacency == null) return null;

        float[] vertices = new float[8];

        switch (adjacency) {
            // Straight connections
            case TOP:
            case BOTTOM:
                vertices = MapCell.extractVertices(from, to,
                    MapCell.CELL_BITS.LEFT,
                    MapCell.CELL_BITS.RIGHT);
                break;
            case LEFT:
            case RIGHT:
                vertices = MapCell.extractVertices(from, to,
                    MapCell.CELL_BITS.TOP,
                    MapCell.CELL_BITS.BOTTOM);
                break;
            // Diagonal connections
            case TOP_LEFT:
                vertices = MapCell.extractVertices(from, to,
                    MapCell.CELL_BITS.TOP_RIGHT,
                    MapCell.CELL_BITS.BOTTOM_LEFT);
                break;
            case TOP_RIGHT:
                vertices = MapCell.extractVertices(from, to,
                    MapCell.CELL_BITS.TOP_LEFT,
                    MapCell.CELL_BITS.BOTTOM_RIGHT);
                break;
            case BOTTOM_LEFT:
                vertices = MapCell.extractVertices(from, to,
                    MapCell.CELL_BITS.BOTTOM_RIGHT,
                    MapCell.CELL_BITS.TOP_LEFT);
                break;
            case BOTTOM_RIGHT:
                vertices = MapCell.extractVertices(from, to,
                    MapCell.CELL_BITS.BOTTOM_LEFT,
                    MapCell.CELL_BITS.TOP_RIGHT);
                break;
        }
        return vertices;
    }
}
