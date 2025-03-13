package com.tin.game.system;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.BSpline;
import com.badlogic.gdx.math.Vector2;

public class CarDrawer {
    float speed = 75.0f;
    float time = 0;
    float path = 1;

    Sprite car;
    BSpline<Vector2> spline;
    SpriteBatch drawBatch;
    OnDelivery onDelivery;

    boolean isDelivering = true;

    @FunctionalInterface
    public interface OnDelivery {
        void deliverToStore();
    }

    public CarDrawer(SpriteBatch drawBatch, Sprite car, BSpline<Vector2> spline, OnDelivery onDelivery) {
        this.car = car;
        this.spline = spline;
        this.drawBatch = drawBatch;
        this.onDelivery = onDelivery;
    }

    public void translateCarAlongSpline(float delta) {
        if(spline.controlPoints == null) return;


        Vector2 dxTime = new Vector2();
        Vector2 dxPath = new Vector2();
        Vector2 out = new Vector2();
        Vector2 angle = new Vector2();

        spline.derivativeAt(dxTime, time);
        spline.derivativeAt(dxPath, path);
        time += (delta * speed / spline.spanCount) / dxTime.len();


        if(time >= 0 && time < 1.0f) {
            path -= (delta * speed / spline.spanCount) / dxPath.len();
            if(!isDelivering) {
                isDelivering = true;
            }
        }
        if(time >= 1.0f) {
            path += (delta * speed / spline.spanCount) / dxPath.len();
            if(isDelivering) {
                onDelivery.deliverToStore();
                isDelivering = false;
            }
        } // time 1, 1.1, 1.2, 2
        if(path > 1) time = 0;

        spline.derivativeAt(angle, 1 - path);
        spline.valueAt(out, 1 - path);

        float facing = angle.angleDeg();

        car.flip(true, true);
        car.setPosition(out.x, out.y);
        car.setRotation(facing);
        car.draw(drawBatch);
    }

    public void dispose() {
        drawBatch.dispose();
    }
}
