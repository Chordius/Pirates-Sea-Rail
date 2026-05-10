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

            System.out.println("Loading Object: " + name);

            // 1. SAFELY CHECK BOTH "class" AND "type"
            // Tiled newer versions use "class", older versions / LibGDX compat uses "type"
            String type = rectObj.getProperties().get("class", String.class);

            if (type == null) {
                type = rectObj.getProperties().get("type", String.class);
            }

            if (type == null) {
                System.err.println("ERROR: Objek '" + name + "' tidak punya field Class/Type di Tiled!");
                return null;
            }

            System.out.println(" -> Class/Type found: " + type);

            switch (type) {
                case "Gate":
                    Object rawX = rectObj.getProperties().get("targetX");
                    float targetX = 0;
                    if (rawX != null) {
                        targetX = Float.parseFloat(rawX.toString());
                    }

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

                case "MapEvent":
                    // Note: We use rectObj here for consistency
                    String scriptId = rectObj.getProperties().get("script_id", String.class);

                    Boolean solidProp = rectObj.getProperties().get("is_solid", Boolean.class);
                    boolean isSolid = (solidProp != null) ? solidProp : true;

                    // Read the File path and extract ONLY the filename
                    String rawPath = rectObj.getProperties().get("sprite_sheet", String.class);
                    String spriteSheet = "";
                    if (rawPath != null) {
                        spriteSheet = new java.io.File(rawPath).getName();
                    }

                    Boolean staticProp = rectObj.getProperties().get("is_static", Boolean.class);
                    boolean isStatic = (staticProp != null) ? staticProp : true;

                    Integer charIdxProp = rectObj.getProperties().get("character_index", Integer.class);
                    int characterIndex = (charIdxProp != null) ? charIdxProp : 0;

                    Float baseSpeedProp = rectObj.getProperties().get("base_speed", Float.class);
                    float baseSpeed = (baseSpeedProp != null) ? baseSpeedProp : 50;

                    return new MapEvent(name, bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight())
                        .scriptId(scriptId)
                        .solid(isSolid)
                        .spriteSheetName(spriteSheet)
                        .isStatic(isStatic)
                        .characterIndex(characterIndex)
                        .baseSpeed(baseSpeed);

                default:
                    System.out.println("Warning: Unknown Object Class -> " + type);
                    return null;
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
