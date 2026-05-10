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
import com.chronicorn.frontend.objects.EnemyMapEvent;
import com.chronicorn.frontend.objects.InteractiveObject;
import com.chronicorn.frontend.objects.MapEvent;
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

    private void loadMapData(String level) {
        if (map != null) map.dispose();
        if (mapRenderer != null) mapRenderer.dispose();

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
        ResetManager.getInstance().resetRoomTimer();

        if (currentScript != null && eventManager != null) {
            currentScript.onMapLoad(eventManager);
        }
    }

    public void changeLevel(String level) {
        loadMapData(level);
        spawnPlayer(); // Spawns based on the Logic layer's "Spawn" object
    }

    public void changeLevel(String level, float x, float y) {
        loadMapData(level);

        // Spawns based on explicit coordinates
        float targetX = x * 48;
        float targetY = (mapManager.getMapHeight() - 1 - y) * 48;
        if (player != null) {
            player.setPosition(targetX, targetY);
        }
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
        if (events.isBusy()) return;

        com.badlogic.gdx.math.Rectangle playerBounds = player.getBounds();
        Array<InteractiveObject> interactables = mapManager.getInteractiveObjects();

        if (interactables == null) return;

        for (InteractiveObject obj : interactables) {
            // 1. TOUCH TRIGGER (Floor plates, cutscene zones)
            if (!obj.isSolid() && playerBounds.overlaps(obj.getBounds())) {
                obj.interact(player, events);
                return; // Stop checking after triggering one event
            }

            // 2. ACTION BUTTON TRIGGER (NPCs, Signs, Chests)
            else if (obj.isSolid() && isPlayerNear(playerBounds, obj.getBounds())) {
                if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.Z)) {
                    obj.interact(player, events);
                    return; // Stop checking after triggering one event
                }
            }
        }
    }

    private boolean isPlayerNear(com.badlogic.gdx.math.Rectangle playerBounds, com.badlogic.gdx.math.Rectangle objBounds) {
        // Expand the object's hitbox by 15 pixels in all directions to create a "proximity zone"
        // This allows the player to interact without needing pixel-perfect overlap
        com.badlogic.gdx.math.Rectangle interactZone = new com.badlogic.gdx.math.Rectangle(
            objBounds.x - 24,
            objBounds.y - 24,
            objBounds.width + 32,
            objBounds.height + 32
        );
        return interactZone.overlaps(playerBounds);
    }

    public void applySolidObjectPhysics(float delta, PhysicsObjects player) {
        if (mapManager != null) {
            mapManager.applySolidObjectPhysics(delta, player);
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

    public void handlePlayerOverworldStrike(float delta, EventManager events) {
        // Only trigger on the exact frame Z is pressed
        if (!Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.Z)) return;
        if (events.isBusy()) return;

        com.badlogic.gdx.math.Rectangle playerBounds = player.getBounds();
        com.badlogic.gdx.math.Rectangle strikeBox = new com.badlogic.gdx.math.Rectangle();

        // 1. Position the Strike Box based on player direction
        // (Assuming you have an enum or int for facing direction: 0=Up, 1=Down, 2=Left, 3=Right)
        float reach = 40f; // How far the weapon reaches
        float thickness = 40f; // How wide the weapon swing is

        // Example logic (you will need to adapt this to how you store player direction)
        /*
        switch(player.getDirection()) {
            case UP:    strikeBox.set(playerBounds.x, playerBounds.y + playerBounds.height, thickness, reach); break;
            case DOWN:  strikeBox.set(playerBounds.x, playerBounds.y - reach, thickness, reach); break;
            case LEFT:  strikeBox.set(playerBounds.x - reach, playerBounds.y, reach, thickness); break;
            case RIGHT: strikeBox.set(playerBounds.x + playerBounds.width, playerBounds.y, reach, thickness); break;
        }
        */

        // Let's just do a generic box around the player for this example if you don't have directions yet
        strikeBox.set(playerBounds.x - 20, playerBounds.y - 20, playerBounds.width + 40, playerBounds.height + 40);

        // 2. Play the weapon swing animation/sound (VFX only, no physics objects!)
        // player.playSwingAnimation();

        // 3. Check if the strike box hit any interactive objects
        Array<InteractiveObject> interactables = mapManager.getInteractiveObjects();
        for (InteractiveObject obj : interactables) {
            if (strikeBox.overlaps(obj.getBounds())) {

                // If it's an enemy, trigger the advantage strike!
                if (obj instanceof EnemyMapEvent) {
                    ((EnemyMapEvent) obj).strikeAdvantage(player, events);
                    return; // Stop checking
                }
            }
        }
    }

    public MapScript getCurrentScript() {
        return this.currentScript;
    }

    public void updateObjects(float delta) {
        Array<InteractiveObject> interactables = mapManager.getInteractiveObjects();
        if (interactables == null) return;

        // MapEvent movement/animation must tick every frame, independent of EventManager command lifetime.
        for (InteractiveObject obj : interactables) {
            if (obj instanceof MapEvent) {
                ((MapEvent) obj).update(delta);
            }
        }
    }

    public void renderYSorted(com.badlogic.gdx.graphics.g2d.SpriteBatch batch, Player player) {
        if (mapManager == null) return;

        com.badlogic.gdx.utils.Array<InteractiveObject> objects = mapManager.getInteractiveObjects();
        if (objects == null) return;

        // 1. Sort all map objects from highest Y to lowest Y (Top to Bottom)
        // LibGDX Array sorting is highly optimized for this.
        objects.sort((o1, o2) -> Float.compare(o2.getBounds().y, o1.getBounds().y));

        boolean playerDrawn = false;

        // 2. Iterate through the sorted objects
        for (InteractiveObject obj : objects) {

            // If the player is "higher" on the screen (larger Y) than this object,
            // the player is standing "behind" it. So, we must draw the player FIRST.
            if (!playerDrawn && player.getBounds().y > obj.getBounds().y) {
                player.render(batch);
                playerDrawn = true;
            }

            // Draw the map object
            obj.render(batch);
        }

        // 3. If the player has the lowest Y of all (standing closest to the bottom edge of the screen),
        // the loop will finish without drawing them. Draw them last so they overlap everything!
        if (!playerDrawn) {
            player.render(batch);
        }
    }
}
