package com.chronicorn.frontend.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Rectangle;

public class ObjectFactory {

    public static InteractiveObject createObject(MapObject mapObj) {
        if (mapObj instanceof RectangleMapObject) {
            RectangleMapObject rectObj = (RectangleMapObject) mapObj;
            String name = rectObj.getName();
            Rectangle bounds = rectObj.getRectangle();

            // DEBUG PRINT
            System.out.println("Loading Object: " + name);

            // 1. Cek Property "type"
            String type = null;
            if (rectObj.getProperties().containsKey("type")) {
                type = (String) rectObj.getProperties().get("type");
            }

            if (type == null) {
                System.err.println("ERROR: Objek '" + name + "' tidak punya custom property 'type' di Tiled!");
                return null;
            }

            System.out.println(" -> Type found: " + type);

            switch (type) {
                case "Gate":
                    Object rawX = rectObj.getProperties().get("targetX");
                    float targetX = 0;
                    if (rawX != null) {
                        targetX = Float.parseFloat(rawX.toString());
                    }

                    // 2. Safe extraction for Y
                    Object rawY = rectObj.getProperties().get("targetY");
                    float targetY = 0;
                    if (rawY != null) {
                        targetY = Float.parseFloat(rawY.toString());
                    }
                    String targetMapName = (String) rectObj.getProperties().get("mapName");
                    return new Gate(name, bounds.getX(), bounds.getY())
                        .setTargetX(targetX)
                        .setTargetY(targetY)
                        .setMapName(targetMapName);

                case "Lever":
                    String target = (String) rectObj.getProperties().get("target");
                    return new Lever(name, bounds.getX(), bounds.getY(), target);

                case "Chest":
                    return new Chest(name, bounds.getX(), bounds.getY());

                case "BreakableWall":
                    return new BreakableWall(name, bounds.getX(), bounds.getY());
            }
        } else if (mapObj instanceof TiledMapTileMapObject) {
            TiledMapTileMapObject tileObj = (TiledMapTileMapObject) mapObj;

            // Extract the visual data from Tiled!
            String type = (String) tileObj.getProperties().get("type");
            TextureRegion region = tileObj.getTile().getTextureRegion();
            String name = tileObj.getName();

            // Create bounds based on where the tile was placed
            float x = tileObj.getX();
            float y = tileObj.getY();
            // Note: Tiled objects scale based on properties, but defaulting to region size is safe usually
            float w = region.getRegionWidth();
            float h = region.getRegionHeight();
            Rectangle bounds = new Rectangle(x, y, w, h);

            switch (type) {
                case "Vase":
                    return new Vase(name, bounds.getX(), bounds.getY(), region);

                default:
                    System.out.println("Unknown object type: " + type);
                    return null;
            }
        }

        return null;
    }
}
