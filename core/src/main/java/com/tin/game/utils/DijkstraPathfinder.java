package com.tin.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.tin.game.core.PathGroup;

import java.util.Comparator;
import java.util.PriorityQueue;

import static com.tin.game.core.PathMap.SubPath;
import static com.badlogic.gdx.utils.IntSet.IntSetIterator;

public class DijkstraPathfinder {

    public DijkstraPathfinder() {

    }

    private interface ToIntFunction<T> extends java.util.function.ToIntFunction<T> {
        int get(T key, int defaultValue);

        @Override
        default int applyAsInt(T value) {
            return get(value, 0);
        }
    }

    public static Array<SubPath> dijkstraShortestPath(Position nodeA, Position nodeB, PathGroup pathGroup) {
        if (pathGroup == null || !pathGroup.containCell(nodeA) || !pathGroup.containCell(nodeB)) {
            return null; // No path exists if they are in different PathGroups
        }

        PriorityQueue<Node<SubPath>> pq = new PriorityQueue<>();

        // distance from nodeA to Position
        ObjectIntMap<Position> distances = new ObjectIntMap<>();

        // Array to store the path in order
        ObjectMap<Position, SubPath> previousPath = new ObjectMap<>();

        pq.add(new Node<>(nodeA, 0, null));
        distances.put(nodeA, 0);

        while (!pq.isEmpty()) {
            Node<SubPath> current = pq.poll();
            Position currentPos = current.position;

            // If we reached the destination, reconstruct the path
            if (currentPos.equals(nodeB)) {
                return reconstructPath(nodeB, previousPath);
            }

            // from current path (nodeA)
            IntSetIterator pathInNode = pathGroup.allCell.get(currentPos).iterator();

            while (pathInNode.hasNext) {
                int pathID = pathInNode.next();

                SubPath subPath = pathGroup.allPath.get(pathID);
                Position nextPos = subPath.getOppositeEnd(currentPos); // Get the other end of the path
                int newCost = distances.get(currentPos, 0) + subPath.getPathLength();

                if (!distances.containsKey(nextPos) || newCost < distances.get(nextPos, 0)) {
                    distances.put(nextPos, newCost);
                    previousPath.put(nextPos, subPath);
                    pq.add(new Node<>(nextPos, newCost, subPath));
                }
            }
        }
        return new Array<>(); // No path found
    }

    /**@deprecated  */
    public static Array<SubPath> findShortestPath(Position nodeA, Position nodeB, PathGroup pathGroup) {
        if (!pathGroup.containCell(nodeA) || !pathGroup.containCell(nodeB)) {
            return null; // No path exists if they are in different PathGroups
        }


        // distance from nodeA to Position
        ObjectIntMap<Position> distances = new ObjectIntMap<>();

        // Initialize priority queue
        PriorityQueue<Position> openSet = new PriorityQueue<>(Comparator.comparingInt((ToIntFunction<Position>) distances::get));

        // Array to store the path in order
        Array<SubPath> pathOrder = new Array<>();

        // Set the starting node
        distances.put(nodeA, 0);
        openSet.add(nodeA);

        while (!openSet.isEmpty()) {
            Position current = openSet.poll();

            // edge case: we reach end node
            if (current.equals(nodeB)) {
                Gdx.app.log("path", "ending path: " + current);
                return reconstructPathOrder(pathOrder, nodeA, nodeB);
                // return pathOrder;
            }

            Gdx.app.log("path", "indexing path: " + current);

            // from current path (nodeA)
            IntSetIterator pathInNode = pathGroup.allCell.get(current).iterator();


            while (pathInNode.hasNext) {
                int pathID = pathInNode.next();

                SubPath path = pathGroup.allPath.get(pathID);
                Position neighbor = path.getStart().equals(current)? path.getEnd() : path.getStart();
                int moveCost = path.getPathLength();
                int newDistance = distances.get(current, 0) + moveCost;

                Gdx.app.log("path", "tracing path: " + path.getStart() + " -> " + path + " <- " + path.getEnd());
                Gdx.app.log("path", "to path: " + neighbor);

                if(path.getEnd() == null) {
                    continue;
//                    Position end = path.getCellVertices().orderedItems().get(path.getCellVertices().size - 1);
//                    neighbor = end;
//                    path.endPath(end);
                }

                if (!distances.containsKey(neighbor) || newDistance < distances.get(neighbor, 0)) {
                    distances.put(neighbor, newDistance);
                    pathOrder.add(path);
                    openSet.add(neighbor);
                }
                //IntSetIterator adjacentPath = pathGroup.adjacentPath.get(pathID).iterator();
            }
        }

        Gdx.app.log("path", "NO PATH FOUND");
        return null; // No path found
    }


    /**@deprecated  */
    private static Array<SubPath> reconstructPathOrder(Array<SubPath> pathOrder, Position nodeA, Position nodeB) {
        Array<SubPath> path = new Array<>();

        // Find the path from nodeA to nodeB in the pathOrder list
        Position following = null;
        for (SubPath subPath : pathOrder) {
            if(subPath.getStart().equals(nodeA)) {
                following = subPath.getEnd();
                path.add(subPath);
            }
            if (subPath.getStart().equals(following)) {
                following = subPath.getEnd();
                path.add(subPath);
            }

            if (subPath.getEnd().equals(nodeB)) {
                path.add(subPath);
                break;
            }
        }

        return path;
    }

    private static Array<SubPath> reconstructPath(Position nodeB, ObjectMap<Position, SubPath> previousPath) {
        Array<SubPath> path = new Array<>();
        Position current = nodeB;

        while (previousPath.containsKey(current)) {
            SubPath subPath = previousPath.get(current);
            path.add(subPath);
            current = subPath.getOppositeEnd(current); // Move backward in the path
        }

        path.reverse();
        return path;
    }
}
