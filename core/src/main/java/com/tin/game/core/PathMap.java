package com.tin.game.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.OrderedSet;
import com.tin.game.utils.Edge;
import com.tin.game.utils.Position;
import com.tin.game.utils.SimplePath;

public class PathMap {
    // Constants for integer-based path calculation
    private static final int STRAIGHT_COST = 100;
    private static final int DIAGONAL_COST = 141; // Approximate √2 * 100 = 141

    /// A PathGroup has its unique ID
    /// ID -> PathGroup

    private int nextID = 0;
    private final IntMap<PathGroup> pathGroup;
    private final PathMapTraverser traverser;

    public PathMap(PathMapTraverser traverser) {
        pathGroup = new IntMap<>();
        this.traverser = traverser;
    }

    public void init() {
        traverser.initTraverser(this::createNewGroup);
    }

    public PathGroup createNewGroup() {
        PathGroup group = new PathGroup();
        pathGroup.put(nextID, group);
        nextID++;
        return group;
    }

    public PathGroup getGroup(int groupID) {
        return  pathGroup.get(groupID);
    }

    public ObjectSet<Edge> getVisitedEdges() {
        return traverser.getVisitedEdges();
    }

    public ObjectSet<Position> getVisitedNodes() {
        return traverser.getVisitedNodes();
    }

    public PathMapTraverser getTraverser() {
        return this.traverser;
    }

    public PathGroup getGroup(Position node) {
        IntMap.Values<PathGroup> itr = pathGroup.values();
        while(itr.hasNext()) {
            PathGroup next = itr.next();
            if(next.allCell.containsKey(node)) {
                return next;
            }
        }
        return null;
    }

    public void deleteGroup() {

    }

    public IntMap.Values<PathGroup> allGroups() {
        return pathGroup.values();
    }

    public void clearAllPath() {
        pathGroup.clear();
    }

    public static class SubPath extends SimplePath {
        public Color debugColor;

        public SubPath(MapCell start) {
            super(start);

            RandomXS128 random = new RandomXS128();
            debugColor = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1.0f);
        }

        @Override
        protected int getDistance(int col, int row, Position last) {
            int distanceX = Math.abs(col - last.col);
            int distanceY = Math.abs(row - last.row);

            if (distanceX == 1 && distanceY == 1)
                return DIAGONAL_COST; // Diagonal move
            else return STRAIGHT_COST; // Straight move
        }

        public static int calculatePathLength(SubPath path) {
            int length = 0;
            OrderedSet<Position> vertices = path.getCellVertices();
            for (int i = 1; i < vertices.size; i++) {
                Position prev = vertices.orderedItems().get(i - 1);
                Position curr = vertices.orderedItems().get(i);

                int dx = Math.abs(curr.col - prev.row);
                int dy = Math.abs(curr.row - prev.col);

                if (dx == 1 && dy == 1)
                    length += DIAGONAL_COST; // Diagonal move
                else length += STRAIGHT_COST; // Straight move
            }
            return length;
        }
    }
}
