package com.tin.game.system;

import com.badlogic.gdx.Gdx;
import com.tin.game.utils.Position;

public class Store extends Position {

    private int currentDelivery = 0;
    private final int requiredDelivery;

    public Store(int col, int row, int requiredDelivery) {
        super(col, row);
        this.requiredDelivery = requiredDelivery;
    }

    public Store(Position fromPos) {
        super(fromPos.col, fromPos.row);
        this.requiredDelivery = 0;
    }

    public void deliver() {
        this.currentDelivery += 1;
        Gdx.app.log("dev", "delivered");
    }

    public boolean isFull() {
        return currentDelivery == requiredDelivery;
    }

    public int needed() {
        return requiredDelivery - currentDelivery;
    }
}
