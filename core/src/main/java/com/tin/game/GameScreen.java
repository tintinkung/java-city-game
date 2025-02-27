package com.tin.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.MathUtils;
import space.earlygrey.shapedrawer.ShapeDrawer;


public class GameScreen extends ScreenAdapter {

    // CHANGING MAP SIZE RESULT IN DIFFERENT PIXEL DENSITY, SO CHANGE THE DEFAULT DIMENSION TOO
    public static final int MAP_WIDTH = 20; // default dimension for tile width 32 * 20 = 640 (1:1 pixel density)
    public static final int MAP_HEIGHT = 15; // default dimension for tile width 32 * 15 = 480 (1:1 pixel density)
    private static final String PROMPT_TEXT = "Click anywhere to generate a new map";
    private static final Color PROMPT_COLOR = Color.CORAL;
    private static final float PROMPT_FADE_IN = 2f;
    private static final float PROMPT_FADE_OUT = 4f;

    private TiledMap map;
    private OrthographicCamera camera;
    private OrthographicCamera guiCam;
    private Viewport viewport;
    private ScreenViewport screenViewport;
    private OrthogonalTiledMapRenderer renderer;
    private AutoTiler autoTiler;
    private int tileSize;
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

        // road drawing setup
        road = new RoadDrawer();

        // Auto generate a new map
        autoTiler = new AutoTiler(MAP_WIDTH, MAP_HEIGHT, Gdx.files.internal("tileset.json"));
        map = autoTiler.generateMap();
        map.getLayers().get(0).setVisible(false);
        tileSize = autoTiler.getTileWidth();
        MapLayers layers = map.getLayers();



        // DEV ONLY: make debug checkerboard layer
        Gdx.app.log("dev", "automated tile map at layer: " + layers.size());

        TiledMapDebug debugLayer = new TiledMapDebug(MAP_WIDTH, MAP_HEIGHT, tileSize);
        TiledMapTileLayer debugCheckerboard = debugLayer.getCheckerBoard(0.25f);
        layers.add(debugCheckerboard);
        debugCheckerboard.setVisible(true);
        debugCheckerboard.setOpacity(0.5f);

        // Setup map renderer
        final float unitScale = 1f / Math.max(autoTiler.getTileWidth(), autoTiler.getTileHeight());
        renderer = new OrthogonalTiledMapRenderer(map, unitScale);

        // Setup input processor
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                // left/right gutter width or the out-of-bound area desktop window
                float offsetX = (float) (screenViewport.getScreenWidth() - viewport.getScreenWidth()) / 2.0f;
                float offsetY = (float) (screenViewport.getScreenHeight() - viewport.getScreenHeight()) / 2.0f;

                // Remap touch position to what cell a tile get clicked
                int column =  MathUtils.floor(
                    MathUtils.map(viewport.getLeftGutterWidth(),
                    viewport.getLeftGutterWidth() + viewport.getScreenWidth(),
                    0.0f, MAP_WIDTH, screenX)
                );

                int row = MathUtils.floor(
                    MathUtils.map(viewport.getTopGutterHeight(),
                    viewport.getTopGutterHeight() + viewport.getScreenHeight(),
                    0.0f, MAP_HEIGHT, screenY)
                );
                road.pushRoad(autoTiler.getCellAt(column, row));

                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                // Generate a new procedural map on touch event
                if(keycode == Input.Keys.R) {
                    map = autoTiler.generateMap();
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
        road.drawActiveRoad();
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
