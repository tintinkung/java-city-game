package com.tin.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.OrderedSet;

import com.tin.game.core.MapCell;

/**
 * Simple path class
 */
public class SimplePath implements Path<Position> {
    protected final OrderedSet<Position> cellVertices;
    protected final OrderedSet<Vector2> drawVertices;

    protected final Position start;

    protected Position end;
    protected int pathLength;

    public SimplePath(MapCell start) {
        this.start = start.pos;
        this.cellVertices = new OrderedSet<>();
        this.drawVertices = new OrderedSet<>();
        addVertex(start);
        this.end = null;
        this.pathLength = 0;
    }

    public void addVertex(MapCell cell) {
        cellVertices.add(cell.pos);
        drawVertices.add(cell.getCenter());
        updatePathLength(cell.pos.col, cell.pos.row);
    }


    public void endPath(MapCell with) {
        this.end = with.pos;
    }

    public void endPath(Position with) {
        this.end = with;
    }

    protected void updatePathLength(int col, int row) {
        if(cellVertices.size > 1) {
            Position last = cellVertices
                .orderedItems()
                .get(cellVertices.size - 1);
            pathLength += getDistance(col, row, last);
        }
    }

    protected int getDistance(int col, int row, Position last) {
        int distanceX = Math.abs(col - last.col);
        int distanceY = Math.abs(row - last.row);

        if (distanceX > distanceY)
            return 14 * distanceY + 10 * (distanceX - distanceY);
        return 14 * distanceX + 10 * (distanceY - distanceX);
    }

    public Position getStart() {
        return start;
    }

    public Position getEnd() {
        return end;
    }

    public Position getOppositeEnd(Position end) {
        if(end.equals(start)) return getEnd();
        else if(end.equals(this.end)) return getStart();
        else throw new GdxRuntimeException("Trying to get opposite endpoint of a non-end position");
    }

    public OrderedSet<Position> getCellVertices() {
        return cellVertices;
    }

    public OrderedSet<Vector2> getDrawVertices() {
        return drawVertices;
    }

    public int getPathLength() {
        if(this.end == null) {
            Gdx.app.error(
                this.getClass().toString(),
                "Trying to request path length of an unfinished path");
        }
        return pathLength;
    }

    @Override
    public String toString() {
        return cellVertices.toString(", ");
    }

    ///
    /// TODO: figure out interpolation and complete this implementation
    ///

    @Override
    public Position derivativeAt(Position out, float t) {
        return null;
    }

    @Override
    public Position valueAt(Position out, float t) {
        return null;
    }

    @Override
    public float approximate(Position v) {
        return 0;
    }

    @Override
    public float locate(Position v) {
        return 0;
    }

    @Override
    public float approxLength(int samples) {
        return 0;
    }
}
