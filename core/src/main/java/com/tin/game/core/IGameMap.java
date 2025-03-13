package com.tin.game.core;

import com.tin.game.utils.Position;

@FunctionalInterface
public interface IGameMap {

    /**
     * @param row y position
     * @param column x position
     * @throws IndexOutOfBoundsException when cell does not exist
     * @return The MapCell
     */
    MapCell getCellAt(int column, int row);

    /**
     *
     * @param position the cell position in this map
     * @throws IndexOutOfBoundsException when cell does not exist
     * @return The MapCell
     */
    default MapCell getCellAt(Position position) {
        return getCellAt(position.col, position.row);
    }
}
