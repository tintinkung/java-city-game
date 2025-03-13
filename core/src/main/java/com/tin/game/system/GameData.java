package com.tin.game.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.BSpline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedSet;
import com.tin.game.core.*;
import com.tin.game.utils.DijkstraPathfinder;
import com.tin.game.utils.Position;

public class GameData {
    private final GameMap gameMap;
    private final PathMap pathMap;
    private final RoadMap roadMap;
    private final TownSystem townSystem;
    private final PathSystem pathSystem;

    // road pushing
    private int lastRow, lastCol;

    public GameData() {
        this.gameMap = new GameMap(true, false);
        this.roadMap = new RoadMap();
        this.townSystem = new TownSystem(this.gameMap, this::populateTown, this::deleteStore);
        this.pathSystem = new PathSystem(this.gameMap, roadMap::excludeConnection);
        this.pathMap = new PathMap(new PathMapTraverser(this.gameMap, roadMap::getAdjacent));
        this.pathMap.init();
    }

    public IGameMap getDrawMap() {
        return this.gameMap;
    }

    public GameMap getGameMap() {
        return this.gameMap;
    }

    public PathMap getPathMap() {
        return this.pathMap;
    }

    public RoadMap getRoadMap() {
        return this.roadMap;
    }

    public PathSystem getPathSystem() {
        return pathSystem;
    }

    public TownSystem getTownSystem() {
        return this.townSystem;
    }

    public void deleteStore(Color colorID) {
        Array<Position> allCell = this.pathSystem.getCellMap(colorID);
        Position storePos = this.townSystem.getStoreMap().get(colorID);

        MapCell store = this.gameMap.getCellAt(storePos);
        MapCell storeX = this.gameMap.getCellAt(storePos.col + 1, storePos.row);
        MapCell storeY = this.gameMap.getCellAt(storePos.col, storePos.row + 1);
        MapCell storeXY = this.gameMap.getCellAt(storePos.col + 1, storePos.row + 1);

        store.clearCell();
        storeX.clearCell();
        storeY.clearCell();
        storeXY.clearCell();

        this.roadMap.removeRoad(store);
        this.roadMap.removeRoad(storeX);
        this.roadMap.removeRoad(storeY);
        this.roadMap.removeRoad(storeXY);

        allCell.forEach((pos) -> {

            try {
                MapCell cell = this.gameMap.getCellAt(pos);
                cell.clearCell();
                this.roadMap.removeRoad(cell);
            }
            catch (GdxRuntimeException ignored) {}
        });

        this.pathSystem.removePath(colorID);
        this.townSystem.removeCar(colorID);
        this.townSystem.removeHouse(colorID);
        this.townSystem.removeStore(colorID);

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

        MapCell pendingCell = this.gameMap.getCellAt(lastCol,  lastRow);
        MapCell confirmCell = this.gameMap.getCellAt(column, row);


        if(MapCell.isAdjacent(pendingCell, confirmCell)) {
            // CELL_BITS adjacency = MapCell.checkAdjacencyType(latestCell, confirmCell);
            if(confirmCell.getType().id() == MapCell.CELL_TYPE.HIGHWAY.id()) return;

            roadMap.pushRoad(pendingCell, confirmCell);
            traverseAllPath();
        }
        lastCol = column;
        lastRow = row;
    }

    public void populateTown(House newHouse, Store newStore) {
        MapCell cell = this.gameMap.getCellAt(newHouse);
        MapCell road = this.gameMap.getCellAt(newHouse.facing);

        roadMap.pushRoad(cell, road);

        MapCell store = this.gameMap.getCellAt(newStore);
        MapCell storeX = this.gameMap.getCellAt(newStore.col + 1, newStore.row);
        MapCell storeY = this.gameMap.getCellAt(newStore.col, newStore.row + 1);
        MapCell storeXY = this.gameMap.getCellAt(newStore.col + 1, newStore.row + 1);

        roadMap.pushRoad(store, storeX);
        roadMap.pushRoad(storeX, storeXY);
        roadMap.pushRoad(store, storeXY);
        roadMap.pushRoad(storeX, storeY);
        roadMap.pushRoad(storeXY, storeY);
        roadMap.pushRoad(storeY, store);

        cell.setType(MapCell.CELL_TYPE.HOUSE);
        store.setType(MapCell.CELL_TYPE.STORE);
        storeX.setType(MapCell.CELL_TYPE.STORE);
        storeY.setType(MapCell.CELL_TYPE.STORE);
        storeXY.setType(MapCell.CELL_TYPE.STORE);

        traverseAllPath();

    }

    public void traverseAllPath() {
        pathMap.getTraverser().clearTraversalData();
        pathMap.clearAllPath();

        ObjectMap.Keys<Position> allNodes = roadMap.getPositions();

        // traverse existing house first
        townSystem.getAllHouse().forEach(entry ->
            entry.forEach(house -> {
                    Gdx.app.log("dev", "traversing " + house + " to " + house.facing);
                    pathMap.getTraverser().traverseNewPath(new Position(house.col, house.row), "= = = housing group = = =");

                    // Check for path finding after we got the path data
                    Array<PathMap.SubPath> pathFind = DijkstraPathfinder.dijkstraShortestPath(
                        house,
                        house.destination,
                        pathMap.getGroup(house));
                    if(pathFind != null && pathFind.size > 0) {
                        // TODO: think of a better way to get colorID
                        Color colorID = townSystem.getStoreID(new Store(house.destination));

                        pathSystem.pushPath(colorID, pathFind);
                        pathMap.getTraverser().clearTraversalData();

                        townSystem.pushCarDrawer(colorID, house.getCar(), pathSystem.getSpline(colorID));
                    }

                }
            )
        );

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

//        townSystem.getAllHouse().forEach(entry ->
//            entry.forEach(house -> {
//                Gdx.app.log("dev", "path finding " + pathMap.getGroup(house));
//
//
//            })
//        );
    }

}
