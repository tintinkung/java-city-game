package com.tin.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

/** {@link com.badlogic.gdx.Game} implementation shared by all platforms. */
public class Main extends Game {
    private GameScreen gameScreen;

    @Override
    public void create() {
        gameScreen = new GameScreen();
        Gdx.app.log("Log", "Game Created");
        setScreen(gameScreen);
    }

//    @Override
//    public void render() {
//        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
//        batch.begin();
//        batch.draw(image, 140, 210);
//        batch.end();
//    }

    @Override
    public void dispose() {
        super.dispose();
        gameScreen.dispose();
    }
}
