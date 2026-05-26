package com.chronicorn.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.eventcommands.CmdPerformCountdownReset;
import com.chronicorn.frontend.eventcommands.CmdPerformReset;
import com.chronicorn.frontend.managers.assetManager.ImageManager;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.playercommands.Dash;
import com.chronicorn.frontend.playercommands.Move;
import com.chronicorn.frontend.managers.*;
import com.chronicorn.frontend.windows.GameHUD;
import com.chronicorn.frontend.windows.GabWindow;
import com.chronicorn.frontend.windows.WindowMessage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class MapScreen implements Screen {

    // Functional Essentials
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    public float fadeAlpha = 0f;
    private float fadeTarget = 0f; // Where we want to go
    private float fadeSpeed = 0f; // How fast to get there
    public com.badlogic.gdx.graphics.Color fadeColor = new com.badlogic.gdx.graphics.Color(0, 0, 0, 1);
    public boolean autoFadeInOnShow = false;
    public Stage stage;
    private GameHUD gameHUD;

    // Map-Based Windows
    private WindowMessage textWindow;
    private GabWindow flexWindow;

    // Player Essentials
    private Player player;
    private Move moveCommand;
    private Dash dashCommand;

    // Managers
    private EventManager eventManager;

    // World Constants
    private final float WORLD_WIDTH = 910.2f;
    private final float WORLD_HEIGHT = 512;

    // Important Game Flags
    private boolean isGameOverTriggered = false;
    private boolean isCountdownTriggered = false;
    private boolean cameraFollow = true;

    // Camera target movement (for manual scrolling)
    private boolean hasCameraTarget = false;
    private float cameraTargetX, cameraTargetY;
    private float cameraScrollSpeed = 0f;
    private boolean cameraResetToPlayerOnArrive = false;

    private BitmapFont hudFont;
    private com.badlogic.gdx.InputMultiplexer inputMultiplexer;

    public MapScreen() {
        this(true);
    }

    public MapScreen(boolean isNewGame) {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        player = new Player(new Vector2(0, 0));

        // Setup Managers
        eventManager = new EventManager();
        shapeRenderer = new ShapeRenderer();

        // Setup Dialog Box
        stage = new Stage(new ScreenViewport());
        textWindow = new WindowMessage();
        flexWindow = new GabWindow();

        // Setup HUD
        // gameHUD = new GameHUD();
        // stage.addActor(gameHUD);
        stage.addActor(textWindow);
        stage.addActor(flexWindow);
        eventManager.setUI(textWindow, flexWindow);

        // Setup Map
        LevelMapManager.getInstance().setMapScreen(this);
        LevelMapManager.getInstance().setPlayer(player);
        // player.addObserver(gameHUD);
        LevelMapManager.getInstance().setEventManager(eventManager);
        if (isNewGame) {
            LevelMapManager.getInstance().changeLevel("LevelIntro");
        }

        // Setup Input
        this.dashCommand = new Dash(player);
        this.moveCommand = new Move(player);

        inputMultiplexer = new com.badlogic.gdx.InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);

        hudFont = ImageManager.font;
    }

    @Override
    public void render(float delta) {
        if (eventManager.isBusy()) {
            eventManager.update(delta);
            player.isBusy = true; // Halts player movement during events
        } else {
            player.isBusy = false;
            if (!cameraFollow && !hasCameraTarget) {
                cameraFollow = true;
            }
            handleInput();
        }

        player.update(delta);
        LevelMapManager.getInstance().updateObjects(delta);

        if (!player.isDead() && !isGameOverTriggered) {
            ResetManager.getInstance().update(delta);
        }
        if (!eventManager.isBusy()) {
            LevelMapManager.getInstance().checkGhostTriggers(delta);
        }

        // 2. Physics & Collisions
        // Normal wall tiles
        LevelMapManager.getInstance().checkWallCollisions(delta, player);
        // Solid Interactive Objects (NPCs, Chests, etc.)
        LevelMapManager.getInstance().applySolidObjectPhysics(delta, player);

        // 3. Events & Hazards
        LevelMapManager.getInstance().checkInteractableTriggers(delta, eventManager);
        LevelMapManager.getInstance().checkHazardTile(delta);

        // Camera Follow Player
        if (cameraFollow) {
            camera.position.set(player.getPosition().x, player.getPosition().y, 0);
            camera.update();
        } else if (hasCameraTarget) {
            if (cameraResetToPlayerOnArrive && player != null) {
                cameraTargetX = player.getPosition().x;
                cameraTargetY = player.getPosition().y;
            }
            camera.position.x += (cameraTargetX - camera.position.x) * cameraScrollSpeed * delta * 60;
            camera.position.y += (cameraTargetY - camera.position.y) * cameraScrollSpeed * delta * 60;
            camera.update();

            if (Math.abs(camera.position.x - cameraTargetX) < 1f && Math.abs(camera.position.y - cameraTargetY) < 1f) {
                camera.position.x = cameraTargetX;
                camera.position.y = cameraTargetY;
                camera.update();
                hasCameraTarget = false;
                if (cameraResetToPlayerOnArrive) {
                    cameraFollow = true;
                }
            }
        }

        GameSession.getInstance().update(delta);

        // --- DRAW LOGIC ---
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        LevelMapManager.getInstance().renderBackground(camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        LevelMapManager.getInstance().renderYSorted(batch, player);
        batch.end();

        LevelMapManager.getInstance().renderForeground(camera);

        if (autoFadeInOnShow) {
            fadeAlpha -= 2.0f * delta;
            if (fadeAlpha <= 0f) {
                fadeAlpha = 0f;
                autoFadeInOnShow = false;
            }
        }

        if (fadeAlpha > 0) {
            changeScreenFade();
        }

        if (stage != null) {
            stage.act(delta);
            stage.draw();
        }

        if (player.isDead() && !isGameOverTriggered) {
            gameOverLogic();
        }
    }

    private void handleInput() {
        moveCommand.execute();
        if (dashCommand != null) dashCommand.execute();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            SceneManager.getInstance().transitionToMenu(player);
        }

        // Camera Zoom Controls (O / PageUp to Zoom Out, I / PageDown to Zoom In)
        if (Gdx.input.isKeyPressed(Input.Keys.PAGE_UP) || Gdx.input.isKeyPressed(Input.Keys.O)) {
            camera.zoom += 0.02f;
            camera.update();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.PAGE_DOWN) || Gdx.input.isKeyPressed(Input.Keys.I)) {
            camera.zoom = Math.max(0.1f, camera.zoom - 0.02f);
            camera.update();
        }
    }

    public void changeScreenFade() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(fadeColor.r, fadeColor.g, fadeColor.b, fadeAlpha);
        shapeRenderer.rect(camera.position.x - camera.viewportWidth * camera.zoom / 2f,
                camera.position.y - camera.viewportHeight * camera.zoom / 2f,
                camera.viewportWidth * camera.zoom, camera.viewportHeight * camera.zoom);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    public void gameOverLogic() {
        isGameOverTriggered = true;
        eventManager.clear();
        eventManager.queue(new CmdPerformReset(player, eventManager)); // cinema cutscene
    }

    public void countdownOverLogic() {
        isCountdownTriggered = true;
        eventManager.clear();
        eventManager.queue(new CmdPerformCountdownReset(player, eventManager));
    }

    public void resetTrigger() {
        isGameOverTriggered = false;
        isCountdownTriggered = false;
    }

    @Override
    public void show() {
        if (inputMultiplexer != null) {
            Gdx.input.setInputProcessor(inputMultiplexer);
        }
        if (autoFadeInOnShow) {
            fadeAlpha = 1.0f;
            fadeColor.set(com.badlogic.gdx.graphics.Color.BLACK);
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (batch != null)
            batch.dispose();
        if (player != null)
            player.dispose();
        if (shapeRenderer != null)
            shapeRenderer.dispose();
        if (stage != null)
            stage.dispose();
        // Clear input processor so disposed stage doesn't continue receiving events
        try {
            Gdx.input.setInputProcessor(null);
        } catch (Exception ignored) {
        }

        // Reset level/map resources
        LevelMapManager.getInstance().reset();

        // Help the GC by nulling large references
        batch = null;
        player = null;
        shapeRenderer = null;
        stage = null;
    }

    public GameHUD getGameHUD() {
        return this.gameHUD;
    }

    public OrthographicCamera getCamera() {
        return this.camera;
    }

    public boolean isCameraFollow() {
        return this.cameraFollow;
    }

    public void setCameraFollow(boolean follow) {
        this.cameraFollow = follow;
        if (follow) {
            this.hasCameraTarget = false;
        }
    }

    public void setCameraTarget(float targetX, float targetY, float speed, boolean resetToPlayer) {
        this.cameraTargetX = targetX;
        this.cameraTargetY = targetY;
        this.cameraScrollSpeed = speed;
        this.cameraResetToPlayerOnArrive = resetToPlayer;
        this.hasCameraTarget = true;
        this.cameraFollow = false;
    }

    public boolean hasCameraTarget() {
        return hasCameraTarget;
    }

    public void clearCameraTarget() {
        this.hasCameraTarget = false;
    }
}
