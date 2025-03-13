package com.tin.game.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.BSpline;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Timer;
import com.tin.game.core.GameMap;
import com.tin.game.core.MapCell;
import com.tin.game.utils.Edge;
import com.tin.game.utils.Position;

import static com.tin.game.core.MapCell.CELL_TYPE.NONE;
import static com.tin.game.core.MapCell.CELL_TYPE.ROAD;

public class TownSystem {

    private final RandomXS128 random;
    private static final int GROWTH_RATE = 50; // in 1/10th of a second
    private static final int START_DELAY = 1; // in seconds

    private final GameMap map;

    // TODO: maybe figure out better data structure for this
    private final ObjectMap<Color, Array<House>> houseMap;
    private final ObjectMap<Color, CarDrawer> carMap;
    private final ObjectMap<Color, Store> storeMap;

    private static Timer timer;
    private static int tick = 0;

    private final OnPopulate onPopulate;
    private final OnStoreFull onStoreFull;

    public final SpriteBatch carBatch;

    @FunctionalInterface
    public interface OnPopulate {
        void populateTown(House newHouse, Store newStore);
    }

    @FunctionalInterface
    public interface OnStoreFull {
        void deleteStore(Color colorID);
    }

    public TownSystem(GameMap map, OnPopulate onPopulate, OnStoreFull onStoreFull) {
        this.random = new RandomXS128();
        this.houseMap = new ObjectMap<>();
        this.storeMap = new ObjectMap<>();
        this.carMap = new ObjectMap<>();
        this.carBatch = new SpriteBatch();
        this.onPopulate = onPopulate;
        this.onStoreFull = onStoreFull;
        this.map = map;
    }

    public void scheduleTownPopulation() {
        timer = new Timer();
        timer.scheduleTask(new Timer.Task() {

            @Override
            public void run() {
                // Tick every 1 / 10th a seconds
                tick++;

                if(tick > GROWTH_RATE + random.nextInt(30)) { // in 5 seconds +- 3
                    tick = 0;
                    randomizeNewTown();
                    Gdx.app.log("timer", "populating 1");
                }
            }
        }, START_DELAY, 0.1f);
        stopPopulate();
    }

    public void startPopulate() {
        timer.start();
    }

    public void stopPopulate() {
        timer.stop();
    }

    public void pushCarDrawer(Color colorID, Sprite car, BSpline<Vector2> spline) {
        if(!carMap.containsKey(colorID))
            carMap.put(colorID, new CarDrawer(carBatch, car, spline, () -> {
                Store store = storeMap.get(colorID);
                store.deliver();
                if(store.isFull()) {
                    onStoreFull.deleteStore(colorID);
                }
            }));
    }

    public void animateAllCar(float delta) {
        this.carMap.values().forEach((car) -> {
            car.translateCarAlongSpline(delta);
        });
    }


    private void randomizeNewTown() {
        Array<Position> empty = map.getEmptyCells();
        Array<Position> emptyStore = map.getEmptyQuadCells();

        Position house = empty.random();

        Position houseRoad = map.getAdjacentCell(house, NONE, ROAD).random();

        Edge edgePos = new Edge(house, houseRoad);

        empty.removeValue(house, false);

        Position store = emptyStore.random();

        House newHouse = new House(edgePos, store);
        Store newStore = new Store(store.col, store.row, 3 + random.nextInt(10));

        onPopulate.populateTown(newHouse, newStore);

        Color colorID = new Color(
            random.nextFloat(),
            random.nextFloat(),
            random.nextFloat(),
            1.0f);

        if(!houseMap.containsKey(colorID)) {
            houseMap.put(colorID, new Array<>());
        }

        houseMap.get(colorID).add(newHouse);
        storeMap.put(colorID, newStore);
        newHouse.initCarSprite(colorID);
    }

    public ObjectMap<Color, Store> getStoreMap() {
        return storeMap;
    }

    public ObjectMap<Color, Array<House>> getHouseMap() {
        return houseMap;
    }

    public ObjectMap.Values<Array<House>> getAllHouse() {
        return houseMap.values();
    }

    public Array<House> getHousesByID(Color colorID) {
        return houseMap.get(colorID);
    }

    public ObjectMap.Values<Store> getAllStore() {
        return storeMap.values();
    }

    public Color getStoreID(Store store) {
        return storeMap.findKey(store, false);
    }

    public void removeCar(Color colorID) {
        carMap.remove(colorID);
    }

    public void removeStore(Color colorID) {
        storeMap.remove(colorID);
    }

    public void removeHouse(Color colorID) {
        houseMap.remove(colorID);
    }


}
