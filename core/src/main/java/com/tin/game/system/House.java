package com.tin.game.system;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.tin.game.utils.Edge;
import com.tin.game.utils.Position;

public class House extends Position {

    private Sprite car;
    public final Position destination;
    public final Position facing;

    public House(int col, int row, Position facing, Position destination) {
        super(col, row);
        this.destination = destination;
        this.facing = facing;
    }

    public House(Edge edgePos, Position destination) {
        super(edgePos.pos1.col, edgePos.pos1.row);
        this.destination = destination;
        this.facing = new Position(edgePos.pos2.col, edgePos.pos2.row);
    }

    public void initCarSprite(Color color) {
        Pixmap carPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        carPixmap.setColor(color);
        carPixmap.drawPixel(0, 0);
        TextureRegion carTexture = new TextureRegion(new Texture(carPixmap), 50, 50, 16, 8);
        carPixmap.dispose();

        this.car =  new Sprite(carTexture);
    }

    public Sprite getCar() {
        return car;
    }

    //    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) return true;
//        if (!(obj instanceof Position)) return false;
//        House other = (House) obj;
//        return row == other.row && col == other.col;
//    }

}
