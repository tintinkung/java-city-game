package com.tin.game.core;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.ObjectMap;
import com.tin.game.utils.SimplePath;
import com.tin.game.utils.Position;

import static com.tin.game.core.PathMap.SubPath;

public class PathGroup {
    private int pathID = 0;

    // ID -> SubPath all path in this group
    public final IntMap<SubPath> allPath;

    // ID -> [ID, ...] all adjacency path to this SubPath
    public final IntMap<IntSet> adjacentPath;

    // Position -> [ID, ...] all nodes to its corresponding SubPath
    public final ObjectMap<Position, IntSet> allCell;

    public PathGroup() {
        this.allPath = new IntMap<>();
        this.adjacentPath = new IntMap<>();
        this.allCell = new ObjectMap<>();
    }

    public int addNewPath(MapCell start) {
        SubPath path = new SubPath(start);
        pathID++;

        allPath.put(pathID, path);
        putCellIfAbsent(start.pos, new IntSet());
        allCell.get(start.pos).add(pathID);

        adjacentPath.put(pathID, new IntSet());

        return pathID;
    }

    public boolean addVertexToPath(int pathID, MapCell cell) {
        SimplePath path = allPath.get(pathID);

        path.addVertex(cell);
        putCellIfAbsent(cell.pos, new IntSet());

        // vertex may intersect some other path
        IntSet.IntSetIterator itr = allCell.get(cell.pos).iterator();
        while(itr.hasNext) adjacentPath.get(itr.next()).add(pathID);

        return allCell.get(cell.pos).add(pathID);
    }

    private void putCellIfAbsent(Position cell, IntSet defaultValue) {
        if(allCell.containsKey(cell)) return;
        allCell.put(cell, defaultValue);
    }

    public IntMap.Values<SubPath> allPaths() {
        return allPath.values();
    }

    public boolean containCell(Position cell) {
        return allCell.containsKey(cell);
    }
}
