package com.chronicorn.frontend.managers.mapManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.managers.ResetManager;
import com.chronicorn.frontend.managers.SoundManager;
import com.chronicorn.frontend.objects.InteractiveObject;
import com.chronicorn.frontend.objects.PhysicsObjects;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.ScriptRegistry;
import com.chronicorn.frontend.screens.MapScreen;
import com.chronicorn.frontend.scripts.MapScript;

public class LevelMapManager {
    private static final LevelMapManager instance = new LevelMapManager();

    // Managers under LevelMap's Instructions
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private TileMapManager mapManager;
    private MapScript currentScript;
    private EventManager eventManager;
    private MapScreen mapScreen;

    private int[] backgroundLayers;
    private int[] foregroundLayers;
    private Player player;
    private String currentMapName;
    private String lastTriggerName = null;

    private final float WORLD_WIDTH = Gdx.graphics.getWidth();
    private final float WORLD_HEIGHT = Gdx.graphics.getHeight();

    public LevelMapManager() {
        mapManager = new TileMapManager();
    }

    public static LevelMapManager getInstance() {
        return instance;
    }

    public void changeLevel(String level) {
        if (map != null) {
            map.dispose();
        }

        if (mapRenderer != null) {
            mapRenderer.dispose();
        }

        TmxMapLoader loader = new TmxMapLoader();
        map = loader.load("maps/" + level + ".tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);

        currentScript = ScriptRegistry.getScriptForMap(level);
        this.currentMapName = level;

        mapManager.parseTileCollisions(map);
        mapManager.parseLogicLayer(map);
        mapManager.parseObjectsLayer(map);
        backgroundLayers = mapManager.getBackground();
        foregroundLayers = mapManager.getForeground();

        playMusic();
        // --------------------------------------------------------

        this.spawnPlayer();

        if (currentScript != null && eventManager != null) {
            currentScript.onMapLoad(eventManager);
        }

        ResetManager.getInstance().resetRoomTimer();
    }

    public void changeLevel(String level, float x, float y) {
        if (map != null) {
            map.dispose();
        }

        if (mapRenderer != null) {
            mapRenderer.dispose();
        }

        TmxMapLoader loader = new TmxMapLoader();
        map = loader.load("maps/" + level + ".tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);

        currentScript = ScriptRegistry.getScriptForMap(level);
        this.currentMapName = level;

        mapManager.parseTileCollisions(map);
        mapManager.parseLogicLayer(map);
        mapManager.parseObjectsLayer(map);
        backgroundLayers = mapManager.getBackground();
        foregroundLayers = mapManager.getForeground();

        playMusic();
        // --------------------------------------------------------

        float targetX = x * 48;
        float targetY = (mapManager.getMapHeight() - 1 - y) * 48;
        if (player != null) {
            player.setPosition(targetX, targetY);
        }

        if (currentScript != null && eventManager != null) {
            currentScript.onMapLoad(eventManager);
        }

        ResetManager.getInstance().resetRoomTimer();
    }

    public void renderBackground(OrthographicCamera camera) {
        if (mapRenderer == null) return;
        mapRenderer.setView(camera);
        mapRenderer.render(backgroundLayers);
    }

    public void renderForeground(OrthographicCamera camera) {
        if (mapRenderer == null) return;
        mapRenderer.render(foregroundLayers);
    }

    public void renderObjects(SpriteBatch batch) {
        if (mapManager != null) {
            mapManager.renderObjects(batch);
        }
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setEventManager(EventManager em) {
        this.eventManager = em;
    }

    public EventManager getEventManager() {
        if (this.eventManager == null) return null;
        return this.eventManager;
    }

    public void setMapScreen(MapScreen mapScreen) {
        this.mapScreen = mapScreen;
    }

    public MapScreen getMapScreen() {
        return this.mapScreen;
    }

    public String getCurrentMapName() {
        return currentMapName;
    }

    public Player getPlayer() {
        return player;
    }

    public void checkWallCollisions(float delta, PhysicsObjects object) {
        mapManager.checkWallCollisions(delta, object);
    }

    public void checkWallCollisions(float delta, Array<? extends PhysicsObjects> objects) {
        if (objects == null || objects.isEmpty()) return;
        for (PhysicsObjects m : objects) {
            mapManager.checkWallCollisions(delta, m);
        }
    }

    public void checkGhostTriggers(float delta) {
        String hitTrigger = mapManager.checkTriggerCollisions(player.getBounds());
        if (hitTrigger != null && !eventManager.isBusy()) {
            if (!hitTrigger.equals(lastTriggerName)) {
                if (currentScript != null) {
                    currentScript.onTrigger(hitTrigger, eventManager);
                }
                lastTriggerName = hitTrigger;
            }
        } else {
            lastTriggerName = null;
        }
    }

    public void checkInteractableTriggers(float delta, EventManager events) {
        InteractiveObject obj = mapManager.checkObjectCollisions(delta, player);
        if (obj != null && !events.isBusy()) {
            obj.interact(player, eventManager);
        }
    }

    public void checkHazardTile(float delta) {
        mapManager.parseHazardCollisions(map,player);
    }

    public void spawnPlayer() {
        MapLayer logicLayer = map.getLayers().get("Logic");
        if (logicLayer == null) {
            return;
        }

        MapObject spawnPoint = logicLayer.getObjects().get("Spawn");
        if (spawnPoint == null) {
            return;
        } else {
            if (spawnPoint instanceof RectangleMapObject) {
                RectangleMapObject rectObject = (RectangleMapObject) spawnPoint;
                float x = rectObject.getRectangle().x;
                float y = rectObject.getRectangle().y;

                if (player != null) {
                    player.setSpawnPoint(x, y);
                }
            }
        }

        playMusic();
    }

    public TiledMap getMap() {
        return map;
    }

    public InteractiveObject getObjectByName(String name) {
        Array<InteractiveObject> interactiveObjects = mapManager.getInteractiveObjects();
        for (InteractiveObject obj : interactiveObjects) {
            if (obj.getName().equals(name)) {
                return obj;
            }
        }
        return null;
    }

    public void reset() {
        if (map != null) map.dispose();
        if (mapRenderer != null) mapRenderer.dispose();

        map = null;
        mapRenderer = null;
        player = null;
    }

    public void playMusic() {
        if (currentMapName.contains("Boss") || currentMapName.equalsIgnoreCase("LevelBoss")) {
            SoundManager.getInstance().playAmbient("ambients2.wav");

            SoundManager.getInstance().playMusic("song_boss.mp3");
        } else {
            SoundManager.getInstance().playAmbient("ambients1.wav");
            SoundManager.getInstance().playMusic("song_room.mp3");
        }
    }
}
