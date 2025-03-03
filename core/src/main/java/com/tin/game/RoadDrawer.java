package com.tin.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import space.earlygrey.shapedrawer.JoinType;
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

    float speed = 0.010f;
    float current = 0;
    int k = 300; //increase k for more fidelity to the spline
    private Sprite devCar;
    private TextureRegion carTexture;

    OrderedSet<Array<Vector2>> allPath;

    public interface IDrawMap {
        MapCell getCellAt(int column, int row);
    }

    public RoadDrawer(IDrawMap drawMap) {
        this.drawMap = drawMap;
        this.roadMap = new RoadMap();
        this.allPath = new OrderedSet<>();
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

        Pixmap carPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        carPixmap.setColor(Color.RED);
        carPixmap.drawPixel(0, 0);
        carTexture = new TextureRegion(new Texture(carPixmap), 50, 50, 16, 16);
        devCar = new Sprite(carTexture);
        this.devCar.setPosition(100, 100);
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
        OrderedSet<OrderedSet<Position>> visitedEdges) {

        OrderedSet<Position> visitedNodes = new OrderedSet<>();

        allNodes.forEach((node) -> {
            if (!visitedNodes.contains(node)) {
                depthFirstTraversalEdges(node, visitedEdges, visitedNodes);
            }
        });
    }


    public void depthFirstTraversalEdges(
        Position start,
        OrderedSet<OrderedSet<Position>> visitedEdges,
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

    public void drawActiveRoad(float delta) {
        //devCar.draw(drawBatch);
        if(roadMap.size() < 2) return;
        OrderedSet<OrderedSet<Position>> visitedEdges = new OrderedSet<>();

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

            Vector2 centerFrom = cutSegmentStart(from.getCenter(), to.getCenter(), RADIUS);
            Vector2 centerTo = cutSegmentEnd(from.getCenter(), to.getCenter(), RADIUS);

            drawer.setColor(Color.CYAN);

            Array<Vector2> edgePath = new Array<>(true, 2);

            edgePath.add(cutSegmentStart(from.getCenter(), to.getCenter(), RADIUS));
            edgePath.add(cutSegmentEnd(from.getCenter(), to.getCenter(), RADIUS));


            allPath.add(edgePath);

//            if(adjacency !== CELL_BITS.TOP
//            || adjacency !== CELL_BITS.BOTTOM
//            || adjacency !== CELL_BITS.LEFT
//            || adjacency !== CELL_BITS.RIGHT)

            // Vector2 prevFrom = allPath.orderedItems().get(allPath.size - 2);
            // Vector2 prevTo = allPath.orderedItems().get(allPath.size - 1);

//            if(prevTo.hasSameDirection(centerFrom)) {
////                allPath.add(prevTo);
////                allPath.add(centerFrom);
////                allPath.add(centerTo);
//                drawer.line(prevTo, centerFrom);
//            }

            // drawer.line(centerFrom, centerTo);

        });

        animateDebugCar(delta);
    }

    public static Vector2 extendStart(Vector2 from, Vector2 to, float distance) {
        Vector2 dir = new Vector2(to.x - from.x, to.y - from.y).nor(); // Normalize direction
        return from.cpy().sub(dir.scl(distance)); // Move backward by distance
    }

    public static Vector2 extendEnd(Vector2 from, Vector2 to, float distance) {
        Vector2 dir = new Vector2(to.x - from.x, to.y - from.y).nor(); // Normalize direction
        return to.cpy().add(dir.scl(distance)); // Move forward by distance
    }


    public static Vector2 cutSegmentStart(Vector2 from, Vector2 to, float r) {
        Vector2 dir = to.cpy().sub(from).nor();  // Normalize direction
        return from.cpy().add(dir.scl(r));       // Move forward by r
    }

    public static Vector2 cutSegmentEnd(Vector2 from, Vector2 to, float r) {
        Vector2 dir = to.cpy().sub(from).nor();  // Normalize direction
        return to.cpy().sub(dir.scl(r));         // Move backward by r
    }

    public static boolean arePointsCollinear(Vector2 A, Vector2 B, Vector2 C, Vector2 D) {
        // Compute direction vectors
        Vector2 AB = new Vector2(B.x - A.x, B.y - A.y);
        Vector2 BC = new Vector2(C.x - B.x, C.y - B.y);
        Vector2 CD = new Vector2(D.x - C.x, D.y - C.y);

        // Compute cross products
        float cross1 = AB.x * BC.y - AB.y * BC.x;
        float cross2 = BC.x * CD.y - BC.y * CD.x;

        // If both cross products are approximately zero, the points are collinear
        return Math.abs(cross1) < 1e-6 && Math.abs(cross2) < 1e-6;
    }


    private void animateDebugCar(float delta) {

        Array<Array<Vector2>> allEdgePath = allPath.orderedItems();
        OrderedSet<Vector2> pointSet = new OrderedSet<>();

        for (Array<Vector2> edgePath : allEdgePath) {
            drawer.line(edgePath.get(1), edgePath.get(0));
        }

//        pointSet.add(allEdgePath.get(0).get(0));
//        pointSet.add(allEdgePath.get(0).get(1));

        for (int i = 0; i < allEdgePath.size; i++) {
            if(i == 0) continue;
            Vector2 prevFrom = allEdgePath.get(i - 1).get(0);
            Vector2 prevTo = allEdgePath.get(i - 1).get(1);
            Vector2 From = allEdgePath.get(i).get(0);
            Vector2 To = allEdgePath.get(i).get(1);

            if(arePointsCollinear(prevFrom, prevTo, From, To)) {
                //drawer.line(prevTo, From);
                pointSet.add(prevFrom);
                pointSet.add(prevTo);
                pointSet.add(From);
                pointSet.add(To);
            }
            else {
                Bezier<Vector2> connection = new Bezier<>();
                Vector2 bezierFrom = extendEnd(prevTo, prevFrom, RADIUS * -2.0f);
                Vector2 bezierTo = extendStart(To, From, RADIUS * -2.0f);

                connection.set(prevTo, bezierFrom, bezierTo, From);



                pointSet.add(prevFrom);
                pointSet.add(prevTo);

                //Gdx.app.log("dev", "bezier start: " + prevTo.toString());

                Vector2 dv1 = new Vector2();
                Vector2 dv2 = new Vector2();
                Vector2 dv3 = new Vector2();
                Vector2 dv4 = new Vector2();

                //Bezier.cubic(dv1, 0.5f, prevTo, bezierFrom, bezierTo, From, tmp);

                connection.valueAt(dv1, 0.2f);
                connection.valueAt(dv2, 0.4f);
                connection.valueAt(dv3, 0.6f);
                connection.valueAt(dv4, 0.8f);

                pointSet.add(dv1);
                pointSet.add(dv2);
                pointSet.add(dv3);
                pointSet.add(dv4);



                //Gdx.app.log("dev", "bezier end: " + From.toString());

                pointSet.add(From);
                pointSet.add(To);

//                Array<Vector2> debugPath = new Array<>();
//                debugPath.add(prevTo);
//                debugPath.add(dv1);
//                debugPath.add(dv2);
//                debugPath.add(dv3);
//                debugPath.add(dv4);
//                debugPath.add(From);
//
//                drawer.path(debugPath, true);


            }


//            if(prevTo.dst(From) > RADIUS) {
//                Gdx.app.log("dev", "sus");
//            }
        }

        Array<Vector2> points = pointSet.orderedItems();
        drawer.path(points, 1.0f, JoinType.NONE, true);

//        if(points.size <= 2) return;
//
//        current += delta * speed;
//        if(current >= 1)
//            current -= 1;
//        float place = current * k;
//        Vector2 first = points.get((int) place);
//        Vector2 second;
//        if(((int)place+1) < k)
//        {
//            second = points.get((int) place + 1);
//        }
//        else
//        {
//            second = points.get(0); //or finish, in case it does not loop.
//        }
//        float t = place - ((int)place); //the decimal part of place
//        drawBatch.draw(carTexture, first.x + (second.x - first.x) * t, first.y + (second.y - first.y) * t);


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
