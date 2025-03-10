package com.tin.game.core;

public abstract class AbstractDrawer {

    /**
     * Map handler for requesting {@link MapCell}
     */
    protected IMapDrawer drawMap;

    public AbstractDrawer(IMapDrawer drawMap) {
        this.drawMap = drawMap;
    }
}
