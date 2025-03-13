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
import com.badlogic.gdx.utils.CharArray;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.MathUtils;
import com.tin.game.core.GameMap;
import com.tin.game.core.MapCell;
import com.tin.game.system.*;

import static com.tin.game.Config.*;

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
    GameData gameData;

    private RoadDrawer roadDrawer;
    private TownDrawer townDrawer;
    private PathDrawer pathDrawer;

    private float elapsedTime = 0;

    public GameScreen() {
        this.gameData = new GameData();
    }

    @Override
    public void show() {
        super.show();

        // Setup camera
        camera = new OrthographicCamera();
        viewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);

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

        GameMap drawMap = gameData.getGameMap();
        GameMap background = GameMap.newBlankMap(SCREEN_WIDTH, SCREEN_HEIGHT);
        map.getLayers().add(background);
        map.getLayers().add(drawMap);
        drawMap.setVisible(true);
        drawMap.setOffsetX(OFFSET_X);
        drawMap.setOffsetY(-OFFSET_Y);

        // road drawing setup
        TownSystem town = gameData.getTownSystem();
        roadDrawer = new RoadDrawer(drawMap, new Color(0x7f7f7fff));
        townDrawer = new TownDrawer(drawMap, Color.WHITE);
        pathDrawer = new PathDrawer(drawMap, new Color(0,0,0,0.25f));

        // Setup map renderer
        final float unitScale = 1f / Math.max(drawMap.getTileWidth(), drawMap.getTileHeight());
        renderer = new OrthogonalTiledMapRenderer(map, 1f);

        // Setup input processor
        town.scheduleTownPopulation();
        town.startPopulate();
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {

                // Extract game width from screen width ignoring UI
                float uiWidth = (viewport.getScreenWidth() * ((SCREEN_WIDTH - GAME_WIDTH) / (float) SCREEN_WIDTH));
                float uiHeight = (viewport.getScreenHeight() * ((SCREEN_HEIGHT - GAME_HEIGHT) / (float) SCREEN_HEIGHT) );
                float gameWidth = viewport.getScreenWidth() - uiWidth;
                float gameHeight = viewport.getScreenHeight() - uiHeight;
                float offsetX = uiWidth / 2f;
                float offsetY = uiHeight / 2f;

                // Remap touch position to what cell a tile get clicked
                int column =  MathUtils.floor(
                    MathUtils.map(viewport.getLeftGutterWidth() + offsetX,
                    viewport.getLeftGutterWidth() + gameWidth + offsetX,
                    0.0f, MAP_WIDTH, screenX)
                );

                if(column < 0 || column >= MAP_WIDTH) return true;

                int row = MathUtils.floor(
                    MathUtils.map(viewport.getTopGutterHeight() + offsetY,
                    viewport.getTopGutterHeight() + gameHeight + offsetY,
                    0.0f, MAP_HEIGHT, screenY)
                );

                if(row < 0 || row >= MAP_HEIGHT) return true;

                gameData.pushRoad(column, row);

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
        roadDrawer.begin();
        if(gameData.getRoadMap().size >= 2) {
            roadDrawer.drawEachRoadCell(gameData.getRoadMap());
            roadDrawer.drawEachRoadConnection(gameData.getPathMap());
        }
        roadDrawer.end();

        pathDrawer.begin();
        pathDrawer.drawAllPath(gameData.getPathSystem().getAllPath());
        pathDrawer.end();

        gameData.getTownSystem().carBatch.begin();
        gameData.getTownSystem().animateAllCar(delta);
        gameData.getTownSystem().carBatch.end();

        townDrawer.begin();
        townDrawer.drawAllHouse(gameData.getTownSystem().getHouseMap());
        townDrawer.drawAllStore(gameData.getTownSystem().getStoreMap());
        townDrawer.end();

        batch.begin();
        gameData.getTownSystem().getAllStore().forEach((store) -> {
            GameMap drawMap = gameData.getGameMap();

            MapCell pos = drawMap.getCellAt(store);

            font.getData().setScale(1.75f);
            font.draw(batch, String.valueOf(store.needed()), pos.x + TILE_SIZE - 8, pos.y - TILE_SIZE + 8);
        });
        batch.end();

        // Render text prompt
        screenViewport.apply(true);
        // batch.setProjectionMatrix(guiCam.combined);
        batch.begin();
        font.getData().setScale(1.0f);

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
        roadDrawer.dispose();
        batch.dispose();
    }
}
