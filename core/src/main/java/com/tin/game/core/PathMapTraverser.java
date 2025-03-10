package com.tin.game.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectSet;
import com.tin.game.utils.Edge;
import com.tin.game.utils.Position;

public class PathMapTraverser extends AbstractDrawer {
    private final ObjectSet<Edge> visitedEdges;
    private final ObjectSet<Position> visitedNodes;

    // handlers
    private final OnTraversal onTraversal;
    private OnCreate onCreate;

    @FunctionalInterface
    public interface OnTraversal {
        ObjectSet<MapCell> getAdjacent(Position position, ObjectSet<MapCell> defaultValue);
    }

    @FunctionalInterface
    public interface OnCreate {
        PathGroup newGroup();
    }

    public PathMapTraverser(IMapDrawer drawMap, OnTraversal onTraversal) {
        super(drawMap);
        this.onTraversal = onTraversal;
        this.visitedEdges = new ObjectSet<>();
        this.visitedNodes = new ObjectSet<>();
    }

    public void initTraverser(OnCreate onCreate) {
        this.onCreate = onCreate;
    }

    public void traverseNewPath(Position start) {
        if(onCreate == null) throw new GdxRuntimeException("PathMapTraverser has not been initialized");

        if(!visitedNodes.contains(start)) {
            PathGroup group = onCreate.newGroup();
            traverseFromNode(start, group);
        }
    }

    public void traverseNewPath(Position start, String debugMsg) {
        if(onCreate == null) throw new GdxRuntimeException("PathMapTraverser has not been initialized");

        if(!visitedNodes.contains(start)) {
            Gdx.app.log("dev", debugMsg);
            PathGroup group = onCreate.newGroup();
            traverseFromNode(start, group);
        }
    }

    public ObjectSet<Edge> getVisitedEdges() {
        return visitedEdges;
    }

    public ObjectSet<Position> getVisitedNodes() {
        return visitedNodes;
    }

    public void clearTraversalData() {
        visitedEdges.clear();
        visitedNodes.clear();
    }

    private void traverseFromNode(Position start, PathGroup group) {

        int pathID = group.addNewPath(drawMap.getCellAt(start));

        startDFT(start, group, pathID);
    }

    private void startDFT(Position start, PathGroup group, int pathID) {

        // Avoid duplicate node visits
        if(visitedNodes.contains(start)) {
            // but make sure there are no unfinished path
            group.allPath.get(pathID).endPath(start);
            return;
        };
        visitedNodes.add(start);

        ObjectSet<MapCell> adjacency = onTraversal.getAdjacent(start, new ObjectSet<>());
        MapCell intersection = drawMap.getCellAt(start);


        // check for connections
        switch (adjacency.size) {
            case 0:
                throw new GdxRuntimeException("A node has 0 adjacency");
            case 1:
                Gdx.app.log("dev", "end path: " + start + " -> " + adjacency.first().pos );
                group.allPath.get(pathID).endPath(start);
                pathDFT(start, adjacency, group, pathID);
                break;
            case 2:
                pathDFT(start, adjacency, group, pathID);
                break;
            case 3:
            case 4:
            default: {
                // found >3 way intersection
                group.allPath.get(pathID).endPath(start);

                // look at its adjacency
                ObjectSet<MapCell> connection = new ObjectSet<>(adjacency);

                connection.forEach((node) -> {
                    if(node != null && visitedNodes.contains(node.pos)) {
                        Gdx.app.log("dev", "found used node: " + node.pos);
                        connection.remove(node);
                    }
                });

                Gdx.app.log("dev", "intersection: " + connection.size);

                // the only node left in this adjacency is now the un-traversed "branch"
                connection.forEach((subPath) -> {
                    // if(visitedNodes.contains(subPath.pos)) return;

                    Edge edge = new Edge(start, subPath.pos);

                    if(!canAddPath(edge)) return;

                    Gdx.app.log("dev", "new branch at: " + subPath.pos);
                    int nextID = group.addNewPath(intersection);
                    group.addVertexToPath(nextID, intersection);
                    addPath(edge, group, nextID);

                    if(!visitedNodes.contains(subPath.pos))
                        startDFT(subPath.pos, group, nextID);
                    else group.allPath.get(nextID).endPath(subPath.pos);
                });

                break;
            }
        }
    }

    private void pathDFT(Position start, ObjectSet<MapCell> adjacency, PathGroup group, int pathID) {
        adjacency.forEach((cell) -> {
            Edge edge = new Edge(start.col, start.row, cell.pos.col, cell.pos.row);
            if(canAddPath(edge)) {
                addPath(edge, group, pathID);
                startDFT(cell.pos, group, pathID);
            }
            else Gdx.app.log("dev", "cannot add edge: " + edge);
        });
    }

    private void addPath(Position start, Position adjacent, PathGroup group, int pathID) {

        Edge edge = new Edge(start.col, start.row, adjacent.col, adjacent.row);

        if (!visitedEdges.contains(edge)) {
            visitedEdges.add(edge);
            group.addVertexToPath(pathID, drawMap.getCellAt(start));
            group.addVertexToPath(pathID, drawMap.getCellAt(adjacent));

            Gdx.app.log("dev", "traversing: " + start + " -> " + adjacent);
        }
    }

    private void addPath(Edge edge, PathGroup group, int pathID) {

        visitedEdges.add(edge);
        boolean pos1 = group.addVertexToPath(pathID, drawMap.getCellAt(edge.pos1));
        boolean pos2 = group.addVertexToPath(pathID, drawMap.getCellAt(edge.pos2));

        if(pos1) Gdx.app.log("dev", "added vertex: " + edge.pos1);
        if(pos2) Gdx.app.log("dev", "added vertex: " + edge.pos2);
    }

    private boolean canAddPath(Position from, Position to) {
        return !visitedEdges.contains(new Edge(from.col, from.row, to.col, to.row));
    }

    private boolean canAddPath(Edge edge) {
        return !visitedEdges.contains(edge);
    }
}
