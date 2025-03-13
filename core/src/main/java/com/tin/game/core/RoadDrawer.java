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
import com.badlogic.gdx.math.Vector2;
import com.tin.game.utils.DijkstraPathfinder;
import com.tin.game.utils.Position;
import com.tin.game.utils.Vector2D;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.Random;

import com.tin.game.core.MapCell.CELL_BITS;

import static com.tin.game.Config.*;
import static com.tin.game.core.PathMap.SubPath;

// ShaperDrawer based road path drawer

/**
 * @deprecated
 */
public class RoadDrawer extends AbstractDrawer implements Disposable {

    public static final float RADIUS = 10.0f;
    public static final float LINE_WIDTH = 4.0f;

    private final PathMap pathMap;
    private final RoadMap roadMap;

    private SpriteBatch drawBatch;
    private ShapeDrawer drawer;
    private Texture drawTexture;
    private int lastRow, lastCol;

    float speed = 75.0f;
    float time = 0;
    float path = 1;

    int k = 300; //increase k for more fidelity to the spline
    private Sprite devCar;

    private final Random random;
    private Array<Position> debugHouse;
    private final Array<PathMap.SubPath> debugPathFind = new Array<>();
    private final BSpline<Vector2> debugSpline = new BSpline<>();

    public RoadDrawer(IGameMap drawMap) {
        super(drawMap);
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
        TextureRegion carTexture = new TextureRegion(new Texture(carPixmap), 50, 50, 16, 8);
        devCar = new Sprite(carTexture);

        debugHouse = getDebugHouse();
        MapCell house1 = drawMap.getCellAt(debugHouse.get(0));
        MapCell road1 = drawMap.getCellAt(debugHouse.get(1));
        MapCell house2 = drawMap.getCellAt(debugHouse.get(2));
        MapCell road2 = drawMap.getCellAt(debugHouse.get(3));

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

        /// TODO: extract this to a separate class
        ///
        Array<SubPath> pathFind = DijkstraPathfinder.dijkstraShortestPath(
            debugHouse.get(0),
            debugHouse.get(2),
            pathMap.getGroup(debugHouse.get(0)));
        if(pathFind != null && pathFind.size > 0) {
            debugPathFind.addAll(pathFind);

            Gdx.app.log("path", "= = = path finding output = = =");

            OrderedSet<Vector2> dataset = new OrderedSet<>();
            OrderedSet<Position> cellData = new OrderedSet<>();

            pathFind.forEach((path) -> {
                Gdx.app.log("path", path.getStart() + " -> " + path.getEnd() + " " + path.debugColor);

                // Check and reverse if path is not flipped
                Array<Position> latest = cellData.orderedItems();
                Array<Position> cell = path.getCellVertices().orderedItems();
                Array<Vector2> draw = path.getDrawVertices().orderedItems();

                if(latest.size >= 1 && !latest.get(latest.size - 1).equals(path.getStart())) {
                    cell.reverse();
                    draw.reverse();
                }

                dataset.addAll(draw);
                cellData.addAll(cell);
            });

            Array<Vector2> ordered = dataset.orderedItems();
            Vector2[] items = new Vector2[dataset.size + 2];
            items[0] = ordered.get(0);
            for (int i = 0; i < ordered.size; i++) items[i + 1] = ordered.get(i);
            items[dataset.size + 1] = ordered.get(ordered.size - 1);

            debugSpline.set(items, 3, false);
        }
    }

    public void drawActiveRoad(float delta) {
        if(roadMap.size < 2) return;

        drawEachRoadCell();
        drawEachRoadConnection();

        debugPathFinding();

        debugCarSpline();
        translateCarAlongSpline(delta);

        // draw debugging house. todo: remove later
        MapCell house1 = drawMap.getCellAt(debugHouse.get(0));
        MapCell house2 = drawMap.getCellAt(debugHouse.get(2));
        drawer.filledRectangle(house1.x + 4, house1.y - 4, 24, -24, Color.MAGENTA);
        drawer.filledRectangle(house2.x + 4, house2.y - 4, 24, -24, Color.ORANGE);
    }

    private void drawEachRoadCell() {
        roadMap.getPositions().forEach((node) -> {
            Vector2 center = drawMap.getCellAt(node).getCenter();
            drawer.setColor(Color.LIGHT_GRAY);
            drawer.circle(center.x, center.y, RADIUS);
            drawer.setColor(Color.GRAY);
            drawer.filledCircle(center, RADIUS - 2);
        });
    }

    private void drawEachRoadConnection() {
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

    private void debugPathFinding() {
        debugPathFind.forEach((path) -> {
            drawer.setColor(path.debugColor);
            drawer.path(path.getDrawVertices().orderedItems(), 7.0f, true);
            // Gdx.app.log("dev", "drawing path: " + path.toString());
        });
    }

    private void debugCarSpline() {
        if(debugSpline.controlPoints != null && debugSpline.controlPoints.length > 1) {
            for (int i = 1; i < 100; i++) {
                Vector2 vertex1 = new Vector2();
                Vector2 vertex2 = new Vector2();

                debugSpline.valueAt(vertex1, MathUtils.map(0, 99, 0, 1, i - 1));
                debugSpline.valueAt(vertex2, MathUtils.map(0, 99, 0, 1, i));

                drawer.setColor(Color.LIME);
                drawer.line(vertex1, vertex2);
            }
        }
    }

    private void debugAllPath() {
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
    }

    private void translateCarAlongSpline(float delta) {
        if(debugSpline.controlPoints == null) return;

        Vector2 dxTime = new Vector2();
        Vector2 dxPath = new Vector2();
        Vector2 out = new Vector2();
        Vector2 angle = new Vector2();

        debugSpline.derivativeAt(dxTime, time);
        debugSpline.derivativeAt(dxPath, path);
        time += (delta * speed / debugSpline.spanCount) / dxTime.len();


        if(time >= 0 && time < 1.0f) path -= (delta * speed / debugSpline.spanCount) / dxPath.len();
        if(time >= 1.0f) path += (delta * speed / debugSpline.spanCount) / dxPath.len(); // time 1, 1.1, 1.2, 2
        if(path > 1) time = 0;

        debugSpline.derivativeAt(angle, path);
        debugSpline.valueAt(out, path);

        float facing = angle.angleDeg();

        devCar.flip(true, true);
        devCar.setPosition(out.x, out.y);
        devCar.setRotation(facing);
        devCar.draw(drawBatch);
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

        while (road2 == null || drawMap.getCellAt(road2).isOccupied()) {
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
