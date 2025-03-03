package com.tin.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.Objects;
import com.tin.game.MapCell.CELL_BITS;
import com.tin.game.MapCell.CELL_TYPE;
import com.tin.game.MapCell.LANE;
import com.tin.game.RoadMap.Position;

// ShaperDrawer based road path drawer
public class RoadDrawer implements Disposable {

    public static final float RADIUS = 10.0f;
    public static final float LINE_WIDTH = 4.0f;

    private final RoadMap roadMap;
    private final IDrawMap drawMap;

    private SpriteBatch drawBatch;
    private ShapeDrawer drawer;
    private Texture drawTexture;
    private int lastRow, lastCol;


    public interface IDrawMap {
        MapCell getCellAt(int column, int row);
    }

    public RoadDrawer(IDrawMap drawMap) {
        this.drawMap = drawMap;
        this.roadMap = new RoadMap();
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
    }

    public void pushRoad(int column, int row) {

        // exact same position
        if(lastCol == column && lastRow == row) return;

        //if(pending.isOccupied()) return; // Do something later

        // initial click, map is empty
        if(roadMap.isEmpty()) {
            lastCol = column;
            lastRow = row;
            Gdx.app.log("dev", "first click! (" + lastCol + ", " + lastRow + ")");
        }

        MapCell pendingCell = drawMap.getCellAt(lastCol,  lastRow);
        MapCell confirmCell = drawMap.getCellAt(column, row);


        if(MapCell.isAdjacent(pendingCell, confirmCell)) {
            // CELL_BITS adjacency = MapCell.checkAdjacencyType(latestCell, confirmCell);
            roadMap.pushRoad(pendingCell, confirmCell);
        }
        lastCol = column;
        lastRow = row;
    }

    public void depthFirstTraversal(
        ObjectMap.Keys<Position> allNodes,
        ObjectSet<OrderedSet<Position>> visitedEdges) {

        ObjectSet<Position> visitedNodes = new ObjectSet<>();

        allNodes.forEach((node) -> {
            if (!visitedNodes.contains(node)) {
                depthFirstTraversalEdges(node, visitedEdges, visitedNodes);
            }
        });
    }


    public void depthFirstTraversalEdges(
        Position start,
        ObjectSet<OrderedSet<Position>> visitedEdges,
        ObjectSet<Position> visitedNodes) {

        if(visitedNodes.contains(start)) return; // Avoid duplicate node visits

        visitedNodes.add(start);

        for(MapCell adjacency : roadMap.getAdjacency(start, new RoadMap.AdjacencySet())) {
            Position neighbor = adjacency.getPosition();

            OrderedSet<Position> edgeKeys = new OrderedSet<>(2);
            OrderedSet<Position> reverseKeys = new OrderedSet<>(2);

            edgeKeys.addAll(
                new Position(start.col, start.row),
                new Position(neighbor.col, neighbor.row)
            );
            reverseKeys.addAll(
                new Position(neighbor.col, neighbor.row),
                new Position(start.col, start.row)
            );

            if (!visitedEdges.contains(edgeKeys) && !visitedEdges.contains(reverseKeys)) {
                visitedEdges.add(edgeKeys);
                // System.out.println("Edge: " + start + " -> " + neighbor);

                depthFirstTraversalEdges(neighbor, visitedEdges, visitedNodes);
            }
        }
    }

    public void drawActiveRoad() {
        if(roadMap.size() < 2) return;
        ObjectSet<OrderedSet<Position>> visitedEdges = new ObjectSet<>();

        depthFirstTraversal(roadMap.getPositions(), visitedEdges);

        roadMap.getPositions().forEach((node) -> {
            Vector2 center = drawMap.getCellAt(node.col, node.row).getCenter();
            drawer.setColor(Color.GRAY);
            drawer.circle(center.x, center.y, RADIUS);
            drawer.setColor(Color.LIGHT_GRAY);
            drawer.filledCircle(center, RADIUS - 2);
        });

        visitedEdges.forEach((edges) -> {
            Array<Position> toDraw = edges.orderedItems();
            if(toDraw.size == 1) return; // BUG

            Position fromPos = toDraw.get(0);
            Position toPos = toDraw.get(1);

            MapCell from = drawMap.getCellAt(fromPos.col, fromPos.row);
            MapCell to = drawMap.getCellAt(toPos.col, toPos.row);

            float[] vertices = makeAdjacencyVertices(from, to);

            Polygon polygon = new Polygon(vertices);
            polygon.setOrigin(from.getCenter().x, from.getCenter().y);

            // draw connection line
            drawer.setColor(Color.LIGHT_GRAY);
            drawer.filledPolygon(polygon);
            drawer.setColor(Color.GRAY);
            drawer.line(vertices[0], vertices[1], vertices[2], vertices[3]);
            drawer.line(vertices[6], vertices[7], vertices[4], vertices[5]);

            Vector2[] lanes = connectLane(from, to);
            drawer.setColor(Color.CYAN);
            drawer.line(lanes[0], lanes[1]);
            drawer.setColor(Color.ORANGE);
            drawer.line(lanes[2], lanes[3]);


        });
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

    private Vector2[] connectLane(MapCell from, MapCell to) {
        CELL_BITS adjacency = MapCell.checkAdjacencyType(from, to);
        Vector2[] vertices = new Vector2[4];
        if(adjacency == null) return vertices;

        Vector2 lhsFrom = from.getLane(adjacency, LANE.LEFT);
        Vector2 lhsTo = to.getLane(adjacency.opposite(), LANE.RIGHT);
        Vector2 rhsFrom = from.getLane(adjacency, LANE.RIGHT);
        Vector2 rhsTo = to.getLane(adjacency.opposite(), LANE.LEFT);
        vertices[0] = lhsFrom;
        vertices[1] = lhsTo;
        vertices[2] = rhsFrom;
        vertices[3] = rhsTo;
        return vertices;
//
//        switch (Objects.requireNonNull(adjacency)) {
//            // Straight connections
//            case TOP:
//                vertices[0] = from.
//                vertices[1] =
//                vertices[2] =
//                vertices[3] =
//                vertices[4] =
//                vertices[5] =
//                vertices[6] =
//                vertices[7] =
//                break;
//            case BOTTOM:
//                break;
//            case LEFT:
//                break;
//            case RIGHT:
//                break;
//            // Diagonal connections
//            case TOP_LEFT:
//                break;
//            case TOP_RIGHT:
//                break;
//            case BOTTOM_LEFT:
//                break;
//            case BOTTOM_RIGHT:
//                break;
//        }
//        return vertices;
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
