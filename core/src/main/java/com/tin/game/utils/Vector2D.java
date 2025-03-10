package com.tin.game.utils;

import com.badlogic.gdx.math.Vector2;

public class Vector2D extends Vector2 {

    public static Vector2 extendStart(Vector2 from, Vector2 to, float distance) {
        Vector2 dir = new Vector2(to.x - from.x, to.y - from.y).nor(); // Normalize direction
        return from.cpy().sub(dir.scl(distance)); // Move backward by distance
    }

    public static Vector2 extendEnd(Vector2 from, Vector2 to, float distance) {
        Vector2 dir = new Vector2(to.x - from.x, to.y - from.y).nor(); // Normalize direction
        return to.cpy().add(dir.scl(distance)); // Move forward by distance
    }

    public static Vector2 cutSegmentStart(Vector2 from, Vector2 to, float r) {
        Vector2 dir = to.cpy().sub(from).nor();  // Normalize direction
        return from.cpy().add(dir.scl(r));       // Move forward by r
    }

    public static Vector2 cutSegmentEnd(Vector2 from, Vector2 to, float r) {
        Vector2 dir = to.cpy().sub(from).nor();  // Normalize direction
        return to.cpy().sub(dir.scl(r));         // Move backward by r
    }

    public static boolean arePointsCollinear(Vector2 A, Vector2 B, Vector2 C, Vector2 D) {
        // Compute direction vectors
        Vector2 AB = new Vector2(B.x - A.x, B.y - A.y);
        Vector2 BC = new Vector2(C.x - B.x, C.y - B.y);
        Vector2 CD = new Vector2(D.x - C.x, D.y - C.y);

        // Compute cross products
        float cross1 = AB.x * BC.y - AB.y * BC.x;
        float cross2 = BC.x * CD.y - BC.y * CD.x;

        // If both cross products are approximately zero, the points are collinear
        return Math.abs(cross1) < 1e-6 && Math.abs(cross2) < 1e-6;
    }
}
