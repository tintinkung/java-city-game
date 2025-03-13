package com.tin.game.core;

public abstract class AbstractDrawer {

    /**
     * Map handler for requesting {@link MapCell}
     */
    protected IGameMap drawMap;

    public AbstractDrawer(IGameMap drawMap) {
        this.drawMap = drawMap;
    }
}
