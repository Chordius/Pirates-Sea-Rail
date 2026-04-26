package com.chronicorn.frontend.managers.mapManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.objects.InteractiveObject;
import com.chronicorn.frontend.objects.ObjectFactory;
import com.chronicorn.frontend.objects.PhysicsObjects;

public class TileMapManager {
    private Array<Rectangle> collisionRects = new Array<>();
    private Array<RectangleMapObject> triggers = new Array<>();
    private Array<InteractiveObject> interactiveObjects = new Array<>();
    private int[] background;
    private int[] foreground;
    private int mapHeight;
    private int mapWidth;

    public void setupLayers(TiledMap map) {
        int bgIndex = map.getLayers().getIndex("UNDER PLAYER");
        int fgIndex = map.getLayers().getIndex("ABOVE PLAYER");

        if (bgIndex != -1) {
            background = new int[]{ bgIndex };
        } else {
            background = new int[]{};
        }

        if (fgIndex != -1) {
            foreground = new int[]{ fgIndex };
        } else {
            foreground = new int[]{};
        }

        calculateMapDimensions(map.getLayers());

        System.out.println("Map Width is: "  + mapWidth);
        System.out.println("Map Height is: " + mapHeight);
    };

    private void calculateMapDimensions(MapLayers layers) {
        for (MapLayer layer : layers) {
            if (layer instanceof MapGroupLayer) {
                calculateMapDimensions(((MapGroupLayer) layer).getLayers());
                if (this.mapWidth > 0) return;
            }

            else if (layer instanceof TiledMapTileLayer) {
                TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
                this.mapWidth = tileLayer.getWidth();
                this.mapHeight = tileLayer.getHeight();
                return;
            }
        }
    }

    public void parseTileCollisions(TiledMap map) {
        setupLayers(map);
        collisionRects.clear();
        for (int num : background) {
            MapLayer layer = map.getLayers().get(num);

            if (layer instanceof MapGroupLayer) {
                checkCollisionsRecursive(((MapGroupLayer) layer).getLayers());
            } else {
                // Redundant tapi biar cepet
                if (layer instanceof TiledMapTileLayer) {
                    extractTiles((TiledMapTileLayer) layer);
                }
            }
        }
    }

    public void checkCollisionsRecursive(MapLayers layers) {
        for (MapLayer layer : layers) {
            if (layer instanceof MapGroupLayer) {
                checkCollisionsRecursive(((MapGroupLayer) layer).getLayers());
            } else {
                extractTiles((TiledMapTileLayer) layer);
            }
        }
    }

    public void extractTiles(TiledMapTileLayer layer) {
        float tileWidth = layer.getTileWidth();
        float tileHeight = layer.getTileHeight();

        // Loop through every single cell in the map
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);

                // Skip empty cells
                if (cell == null) continue;
                if (cell.getTile() == null) continue;

                // Get the objects defined on this specific tile in the Tileset Editor
                MapObjects objects = cell.getTile().getObjects();

                for (RectangleMapObject rectangleObject : objects.getByType(RectangleMapObject.class)) {
                    Rectangle rect = rectangleObject.getRectangle();

                    Rectangle worldRect = new Rectangle(
                        x * tileWidth + rect.x,
                        y * tileHeight + rect.y,
                        rect.width,
                        rect.height
                    );

                    collisionRects.add(worldRect);
                }
            }
        }
    }

    public void checkWallCollisions(float delta, PhysicsObjects objects, Array<Rectangle> walls) {
        Rectangle playerRect = objects.getBounds();

        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            return;
        }

        // Now uses the passed 'walls' list instead of the class field 'collisionRects'
        for (Rectangle wall : walls) {
            if (Intersector.overlaps(playerRect, wall)) {
                // ... (Your existing collision math stays exactly the same) ...

                Rectangle intersection = new Rectangle();
                Intersector.intersectRectangles(playerRect, wall, intersection);

                if (intersection.width < intersection.height) {
                    if (objects.getPosition().x < wall.x) objects.getPosition().x -= intersection.width;
                    else objects.getPosition().x += intersection.width;
                } else {
                    if (objects.getPosition().y < wall.y) objects.getPosition().y -= intersection.height;
                    else objects.getPosition().y += intersection.height;
                }
                objects.getBounds().setPosition(objects.getPosition().x, objects.getPosition().y);
            }
        }
    }

    public void checkWallCollisions(float delta, PhysicsObjects objects) {
        checkWallCollisions(delta, objects, this.collisionRects);
    }

    public void parseLogicLayer(TiledMap map) {
        triggers.clear();

        MapLayer logicLayer = map.getLayers().get("Logic");

        if (logicLayer == null) {
            System.out.println("Warning: No 'logic' layer found in this map.");
            return;
        }

        // 3. Loop through objects in that layer
        for (MapObject object : logicLayer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                triggers.add((RectangleMapObject) object);
            }
        }
    }

    public String checkTriggerCollisions(Rectangle playerBounds) {
        for (RectangleMapObject obj : triggers) {
            if (Intersector.overlaps(playerBounds, obj.getRectangle())) {
                return obj.getName(); // Return "Chest1", "ExitZone", etc.
            }
        }
        return null;
    }

    public void parseObjectsLayer(TiledMap map) {
        interactiveObjects.clear();

        MapLayer objectsLayer = map.getLayers().get("Objects");

        if (objectsLayer == null) {
            System.out.println("Warning: No 'Objects' layer found in this map.");
            return;
        }

        for (MapObject mapObj : objectsLayer.getObjects()) {
            InteractiveObject gameObj = ObjectFactory.createObject(mapObj);

            if (gameObj != null) {
                interactiveObjects.add(gameObj);
            }
        }
    }

    public InteractiveObject checkObjectCollisions(float delta, PhysicsObjects player) {
        Array<Rectangle> tempCollisionList = new Array<>();

        for (InteractiveObject obj : interactiveObjects) {
            if (Intersector.overlaps(player.getBounds(), obj.getBounds())) {
                if (obj.isSolid()) {
                    tempCollisionList.clear();
                    tempCollisionList.add(obj.getBounds());
                    checkWallCollisions(delta, player, tempCollisionList);
                }
                return obj;
            }
        }
        return null;
    }

    public void parseHazardCollisions(TiledMap map, Player player) {
        for (int layerIdx : background) {
            MapLayer layer = map.getLayers().get(layerIdx);
            recursiveCheckHazard(layer, player);
        }
    }

    private void recursiveCheckHazard(MapLayer layer, Player player) {
        if (layer instanceof MapGroupLayer) {
            MapGroupLayer group = (MapGroupLayer) layer;
            for (MapLayer child : group.getLayers()) {
                recursiveCheckHazard(child, player);
            }
        } else if (layer instanceof TiledMapTileLayer) {
            checkTileLayerForHazard((TiledMapTileLayer) layer, player);
        }
    }

    private void checkTileLayerForHazard(TiledMapTileLayer layer, Player player) {
        // Get center point
        float checkX = player.getBounds().x + player.getBounds().width / 2;
        float checkY = player.getBounds().y + player.getBounds().height / 4;

        // convert world coordinates
        int cellX = (int) (checkX / layer.getTileWidth());
        int cellY = (int) (checkY / layer.getTileHeight());

        // Check Coordinates
        if (cellX < 0 || cellX >= layer.getWidth() || cellY < 0 || cellY >= layer.getHeight()) {
            return;
        }

        // Get the Cell
        TiledMapTileLayer.Cell cell = layer.getCell(cellX, cellY);

        // Check properties
        if (cell != null && cell.getTile() != null) {
            if (cell.getTile().getProperties().containsKey("isHazard")) {
                player.takeHazardDamage(1);
            }
        }
    }

    public void renderObjects(SpriteBatch batch) {
        for (InteractiveObject obj : interactiveObjects) {
            obj.render(batch);
        }
    }

    public int[] getForeground() {
        return foreground;
    }

    public int[] getBackground() {
        return background;
    }

    public Array<InteractiveObject> getInteractiveObjects() {
        return interactiveObjects;
    }

    public float getMapHeight() {
        return mapHeight;
    }

    public float getMapWidth() {
        return mapWidth;
    }

    public void cameraControl() {

    }
}
