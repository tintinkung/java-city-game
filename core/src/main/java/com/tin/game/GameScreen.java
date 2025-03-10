package com.tin.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.MathUtils;
import com.tin.game.core.MapDrawer;
import com.tin.game.core.RoadDrawer;

import static com.tin.game.Config.MAP_HEIGHT;
import static com.tin.game.Config.MAP_WIDTH;

public class GameScreen extends ScreenAdapter {

    private static final String PROMPT_TEXT = "Click anywhere to generate draw road";
    private static final Color PROMPT_COLOR = Color.CORAL;
    private static final float PROMPT_FADE_IN = 2f;
    private static final float PROMPT_FADE_OUT = 4f;

    private TiledMap map;
    private OrthographicCamera camera;
    private OrthographicCamera guiCam;
    private Viewport viewport;
    private ScreenViewport screenViewport;
    private OrthogonalTiledMapRenderer renderer;
    private BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();
    private SpriteBatch batch;

    // road
    private RoadDrawer road;

    private float elapsedTime = 0;

    @Override
    public void show() {
        super.show();

        // Setup camera
        camera = new OrthographicCamera();
        viewport = new FitViewport(MAP_WIDTH, MAP_HEIGHT, camera);

        // Setup GUI camera
        guiCam = new OrthographicCamera();
        screenViewport = new ScreenViewport(guiCam);
        guiCam.setToOrtho(false);

        // Setup font rendering
        batch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal("ui/font.fnt"), false);
        font.setColor(PROMPT_COLOR);
        layout.setText(font, PROMPT_TEXT);

        // map setup with default MapDrawer
        map = new TiledMap();

        MapDrawer drawMap = new MapDrawer(true, false);
        map.getLayers().add(drawMap);
        drawMap.setVisible(true);

        // road drawing setup
        road = new RoadDrawer(drawMap);

        // Setup map renderer
        final float unitScale = 1f / Math.max(drawMap.getTileWidth(), drawMap.getTileHeight());
        renderer = new OrthogonalTiledMapRenderer(map, unitScale);

        // Setup input processor
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {

                // Remap touch position to what cell a tile get clicked
                int column =  MathUtils.floor(
                    MathUtils.map(viewport.getLeftGutterWidth(),
                    viewport.getLeftGutterWidth() + viewport.getScreenWidth(),
                    0.0f, MAP_WIDTH, screenX)
                );

                if(column < 0 || column >= MAP_WIDTH) return true;

                int row = MathUtils.floor(
                    MathUtils.map(viewport.getTopGutterHeight(),
                    viewport.getTopGutterHeight() + viewport.getScreenHeight(),
                    0.0f, MAP_HEIGHT, screenY)
                );

                if(row < 0 || row >= MAP_HEIGHT) return true;

                road.pushRoad(column, row);

                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                // TODO: reset map on R key
                if(keycode == Input.Keys.R) {
                    elapsedTime = 0;
                }

                return true;
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        // renderer viewport
        viewport.update(width, height);

        // entire window viewport
        screenViewport.update(width, height);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render map
        viewport.apply(true);
        renderer.setView(camera);
        renderer.render();

        elapsedTime += delta;

        // road drawing
        road.begin();
        road.drawActiveRoad(delta);
        road.end();

        // Render text prompt
        screenViewport.apply(true);
        batch.setProjectionMatrix(guiCam.combined);
        batch.begin();
        font.setColor(PROMPT_COLOR.r, PROMPT_COLOR.g, PROMPT_COLOR.b,
            (elapsedTime - PROMPT_FADE_IN) % PROMPT_FADE_OUT);
        font.draw(batch, PROMPT_TEXT,
            (screenViewport.getScreenWidth() - layout.width) / 2.0f,
            screenViewport.getScreenHeight() - layout.height);
        batch.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        map.dispose();
        font.dispose();
        road.dispose();
        batch.dispose();
    }
}
