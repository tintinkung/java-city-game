package com.tin.game.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.tin.game.utils.DijkstraPathfinder;
import com.tin.game.utils.Position;
import com.tin.game.utils.Vector2D;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.Random;

import com.tin.game.core.MapCell.CELL_BITS;

import static com.tin.game.Config.*;
import static com.tin.game.core.PathMap.SubPath;

// ShaperDrawer based road path drawer
public class RoadDrawer implements Disposable {


    public static final float RADIUS = 10.0f;
    public static final float LINE_WIDTH = 4.0f;

    private final PathMap pathMap;
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

    private final Random random;
    private Array<Position> debugHouse;
    private final Array<PathMap.SubPath> debugPathFind = new Array<>();

    @FunctionalInterface
    public interface IDrawMap {
        MapCell getCellAt(int column, int row);
    }

    public RoadDrawer(IDrawMap drawMap) {
        this.drawMap = drawMap;
        this.roadMap = new RoadMap();
        this.random = new Random();
        this.pathMap = new PathMap(new PathMapTraverser(this.drawMap, roadMap::getAdjacent));
        this.pathMap.init();
        initDrawer();
    }

    private void initDrawer() {
        drawBatch = new SpriteBatch();

        Pixmap roadPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

        // Main tint, this brightens all the road
        roadPixmap.setColor(new Color(0x7f7f7fff));
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

        debugHouse = getDebugHouse();
        MapCell house1 = drawMap.getCellAt(debugHouse.get(0).col, debugHouse.get(0).row);
        MapCell road1 = drawMap.getCellAt(debugHouse.get(1).col, debugHouse.get(1).row);
        MapCell house2 = drawMap.getCellAt(debugHouse.get(2).col, debugHouse.get(2).row);
        MapCell road2 = drawMap.getCellAt(debugHouse.get(3).col, debugHouse.get(3).row);

        roadMap.pushRoad(house1, road1);
        roadMap.pushRoad(house2, road2);
        traverseAllPath();
    }

    public void pushRoad(int column, int row) {
        // exact same position
        if(lastCol == column && lastRow == row) return;

        //if(pending.isOccupied()) return; // Do something later

        // initial click, map is empty
        if(roadMap.isEmpty()) {
            lastCol = column;
            lastRow = row;

            // dev stuff for debugging
            Gdx.app.log("dev", "first click! (" + lastCol + ", " + lastRow + ")");
        }

        MapCell pendingCell = drawMap.getCellAt(lastCol,  lastRow);
        MapCell confirmCell = drawMap.getCellAt(column, row);


        if(MapCell.isAdjacent(pendingCell, confirmCell)) {
            // CELL_BITS adjacency = MapCell.checkAdjacencyType(latestCell, confirmCell);
            roadMap.pushRoad(pendingCell, confirmCell);
            traverseAllPath();
        }
        lastCol = column;
        lastRow = row;
    }

    public void traverseAllPath() {
        pathMap.getTraverser().clearTraversalData();
        pathMap.clearAllPath();
        debugPathFind.clear();

        ObjectMap.Keys<Position> allNodes = roadMap.getPositions();

        // traverse existing house first
        for (int i = 0; i < debugHouse.size; i++) {
            if(i % 2 == 0) {
                pathMap.getTraverser().traverseNewPath(debugHouse.get(i), "= = = housing group = = =");
            }
        }

        // then look for any disconnected nodes
        allNodes.forEach((node) -> {
            pathMap.getTraverser().traverseNewPath(node, "= = = disconnected group = = =");
        });

        pathMap.allGroups().forEach((group) -> {
            Gdx.app.log("dev", "= = = traversal output = = =");
            group.allPath.values().forEach((path) -> {
                Gdx.app.log(path.debugColor.toString(), path.getStart() + " -> " + path + " <- " + path.getEnd());
                // Gdx.app.log("dev", "drawing path: " + path.toString());
            });
        });

        Array<SubPath> pathFind = DijkstraPathfinder.dijkstraShortestPath(
            debugHouse.get(0),
            debugHouse.get(2),
            pathMap.getGroup(debugHouse.get(0)));
        if(pathFind != null && pathFind.size > 0) {
            debugPathFind.addAll(pathFind);

            Gdx.app.log("path", "= = = path finding output = = =");

            pathFind.forEach((path) -> {
                Gdx.app.log("path", path.getStart() + " -> " + path.getEnd() + " " + path.debugColor);
            });
        }
    }

    public void drawActiveRoad(float delta) {
        //devCar.draw(drawBatch);
        if(roadMap.size < 2) return;

        roadMap.getPositions().forEach((node) -> {
            Vector2 center = drawMap.getCellAt(node.col, node.row).getCenter();
            drawer.setColor(Color.LIGHT_GRAY);
            drawer.circle(center.x, center.y, RADIUS);
            drawer.setColor(Color.GRAY);
            drawer.filledCircle(center, RADIUS - 2);
        });

        pathMap.getVisitedEdges().forEach((edge) -> {
            Position fromPos = edge.pos1;
            Position toPos = edge.pos2;

            MapCell from = drawMap.getCellAt(fromPos.col, fromPos.row);
            MapCell to = drawMap.getCellAt(toPos.col, toPos.row);

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

            Vector2 centerFrom = Vector2D.cutSegmentStart(from.getCenter(), to.getCenter(), RADIUS);
            Vector2 centerTo = Vector2D.cutSegmentEnd(from.getCenter(), to.getCenter(), RADIUS);
        });



        // debugPath();
        //animateDebugCar(delta);

        pathMap.allGroups().forEach((group) -> {
            group.allPath.values().forEach((path) -> {
                drawer.setColor(path.debugColor);
                if(path.getEnd() == null) {
                    drawer.path(path.getDrawVertices().orderedItems(), 7.0f, true);
                }
                else drawer.path(path.getDrawVertices().orderedItems(), 3.0f, true);
                // Gdx.app.log("dev", "drawing path: " + path.toString());
            });
        });


        // draw debugging house. todo: remove later
        MapCell house1 = drawMap.getCellAt(debugHouse.get(0).col, debugHouse.get(0).row);
        MapCell house2 = drawMap.getCellAt(debugHouse.get(2).col, debugHouse.get(2).row);


        // Gdx.app.log("path", "path finding: " + debugPathFind.size);

        debugPathFind.forEach((path) -> {
            drawer.setColor(path.debugColor);
            drawer.path(path.getDrawVertices().orderedItems(), 7.0f, true);
            // Gdx.app.log("dev", "drawing path: " + path.toString());
        });


        drawer.filledRectangle(house1.x + 4, house1.y - 4, 24, -24, Color.MAGENTA);
        drawer.filledRectangle(house2.x + 4, house2.y - 4, 24, -24, Color.ORANGE);
    }


    private void debugPath() {
        //Gdx.app.log("dev", "got path: " + allPath);
        Gdx.app.log("dev", "= = = debugPath = = =");

        pathMap.allGroups().forEach((group) -> {
            group.allPaths().forEach((path) -> {

                Position from = path.getStart();
                Position to = path.getEnd();


                Gdx.app.log("dev", "current path: "
                    + from + " -> " + to);
            });
        });

    }

    private void animateDebugCar(float delta) {

//        Array<Array<Vector2>> allEdgePath = allPath.orderedItems();
//        OrderedSet<Vector2> pointSet = new OrderedSet<>();
//
//        for (Array<Vector2> edgePath : allEdgePath) {
//            drawer.line(edgePath.get(1), edgePath.get(0));
//        }
//
//        pointSet.add(allEdgePath.get(0).get(0));
//        pointSet.add(allEdgePath.get(0).get(1));
//
//        for (int i = 0; i < allEdgePath.size; i++) {
//            if(i == 0) continue;
//            Vector2 prevFrom = allEdgePath.get(i - 1).get(0);
//            Vector2 prevTo = allEdgePath.get(i - 1).get(1);
//            Vector2 From = allEdgePath.get(i).get(0);
//            Vector2 To = allEdgePath.get(i).get(1);
//
//            if(arePointsCollinear(prevFrom, prevTo, From, To)) {
//                //drawer.line(prevTo, From);
//                pointSet.add(prevFrom);
//                pointSet.add(prevTo);
//                pointSet.add(From);
//                pointSet.add(To);
//            }
//            else {
//                Bezier<Vector2> connection = new Bezier<>();
//                Vector2 bezierFrom = extendStart(prevTo, prevFrom, RADIUS);
//                Vector2 bezierTo = extendEnd(To, From, RADIUS);
//
//                connection.set(prevTo, bezierFrom, bezierTo, From);
//
//
//
//                pointSet.add(prevFrom);
//                pointSet.add(prevTo);
//
//                //Gdx.app.log("dev", "bezier start: " + prevTo.toString());
//
//                Vector2 dv1 = new Vector2();
//                Vector2 dv2 = new Vector2();
//                Vector2 dv3 = new Vector2();
//                Vector2 dv4 = new Vector2();
//
//                //Bezier.cubic(dv1, 0.5f, prevTo, bezierFrom, bezierTo, From, tmp);
//
//                connection.valueAt(dv1, 0.2f);
//                connection.valueAt(dv2, 0.4f);
//                connection.valueAt(dv3, 0.6f);
//                connection.valueAt(dv4, 0.8f);
//
//                pointSet.add(dv1);
//                pointSet.add(dv2);
//                pointSet.add(dv3);
//                pointSet.add(dv4);
//
//
//
//                //Gdx.app.log("dev", "bezier end: " + From.toString());
//
//                pointSet.add(From);
//                pointSet.add(To);
//
////                Array<Vector2> debugPath = new Array<>();
////                debugPath.add(prevTo);
////                debugPath.add(dv1);
////                debugPath.add(dv2);
////                debugPath.add(dv3);
////                debugPath.add(dv4);
////                debugPath.add(From);
////
////                drawer.path(debugPath, true);
//
//
//            }
//
//
////            if(prevTo.dst(From) > RADIUS) {
////                Gdx.app.log("dev", "sus");
////            }
//        }
//
//        Array<Vector2> points = pointSet.orderedItems();
//        drawer.path(points, 1.0f, JoinType.NONE, true);

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

    public Array<Position> getDebugHouse() {
        Array<Position> debugHouse = new Array<>(true, 4);

        int x1 = random.nextInt(MAP_WIDTH);
        int y1 = random.nextInt(MAP_HEIGHT);

        MapCell house1 = drawMap.getCellAt(x1, y1);


        Position road1 = MapCell.remapScreenToCell(
            Vector2D.extendEnd(house1.getCenter(), house1.getCorner(random.nextInt(7)), 20.0f)
        );


        while (road1  == null) {
             road1 = MapCell.remapScreenToCell(
                 Vector2D.extendEnd(house1.getCenter(), house1.getCorner(random.nextInt(7)), 20.0f)
            );
        }


        int x2 = random.nextInt(MAP_WIDTH);
        int y2 = random.nextInt(MAP_HEIGHT);


        while (x1 == x2 && y1 == y2) {
            x2 = random.nextInt(MAP_WIDTH);
            y2 = random.nextInt(MAP_HEIGHT);
        }

        MapCell house2 = drawMap.getCellAt(x2, y2);

        Position road2 = MapCell.remapScreenToCell(
            Vector2D.extendEnd(house2.getCenter(), house2.getCorner(random.nextInt(7)), 20.0f)
        );

        while (road2 == null || drawMap.getCellAt(road2.col, road2.row).isOccupied()) {
            road2 = MapCell.remapScreenToCell(
                Vector2D.extendEnd(house2.getCenter(), house2.getCorner(random.nextInt(7)), 20.0f)
            );
        }

        debugHouse.add(new Position(x1, y1));
        debugHouse.add(road1);

        debugHouse.add(new Position(x2, y2));
        debugHouse.add(road2);

        Gdx.app.log("dev", "house1: " + house1.pos);
        Gdx.app.log("dev", "road1: " + road1);
        Gdx.app.log("dev", "house2: " + house2.pos);
        Gdx.app.log("dev", "road2: " + road2);

        return debugHouse;
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
        if(adjacency == null) return null;

        float[] vertices = new float[8];

        switch (adjacency) {
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
