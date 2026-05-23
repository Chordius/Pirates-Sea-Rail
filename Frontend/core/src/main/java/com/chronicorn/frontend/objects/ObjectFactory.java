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

                    boolean isSolid = getBooleanProperty(rectObj.getProperties(), "is_solid", true);

                    // Read the File path and extract ONLY the filename
                    String rawPath = rectObj.getProperties().get("sprite_sheet", String.class);
                    String spriteSheet = "";
                    if (rawPath != null) {
                        spriteSheet = new java.io.File(rawPath).getName();
                    }

                    boolean isStatic = getBooleanProperty(rectObj.getProperties(), "is_static", true);

                    int characterIndex = getIntProperty(rectObj.getProperties(), "character_index", 0);

                    float baseSpeed = getFloatProperty(rectObj.getProperties(), "base_speed", 50f);

                    // Autonomous Movement Properties
                    String rawMoveType = rectObj.getProperties().get("autonomous_move_type", String.class);
                    MapEvent.MoveRouteType moveRouteType = MapEvent.MoveRouteType.STATIC;
                    if (rawMoveType != null) {
                        try {
                            moveRouteType = MapEvent.MoveRouteType.valueOf(rawMoveType.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            System.err.println("Warning: Invalid autonomous_move_type: " + rawMoveType);
                        }
                    }

                    float moveDelay = getFloatProperty(rectObj.getProperties(), "autonomous_move_delay", 2.0f);

                    String rawRoute = rectObj.getProperties().get("autonomous_move_route", String.class);
                    int[] moveRoute = null;
                    if (rawRoute != null && !rawRoute.trim().isEmpty()) {
                        String[] tokens = rawRoute.split(",");
                        moveRoute = new int[tokens.length];
                        for (int i = 0; i < tokens.length; i++) {
                            String token = tokens[i].trim().toUpperCase();
                            if (token.equals("DOWN") || token.equals("0") || token.equals("D")) {
                                moveRoute[i] = 0;
                            } else if (token.equals("LEFT") || token.equals("1") || token.equals("L")) {
                                moveRoute[i] = 1;
                            } else if (token.equals("RIGHT") || token.equals("2") || token.equals("R")) {
                                moveRoute[i] = 2;
                            } else if (token.equals("UP") || token.equals("3") || token.equals("U")) {
                                moveRoute[i] = 3;
                            } else if (token.equals("LOOK_DOWN") || token.equals("LD") || token.equals("FACE_DOWN") || token.equals("FD") || token.equals("TURN_DOWN") || token.equals("TD") || token.equals("4")) {
                                moveRoute[i] = 4;
                            } else if (token.equals("LOOK_LEFT") || token.equals("LL") || token.equals("FACE_LEFT") || token.equals("FL") || token.equals("TURN_LEFT") || token.equals("TL") || token.equals("5")) {
                                moveRoute[i] = 5;
                            } else if (token.equals("LOOK_RIGHT") || token.equals("LR") || token.equals("FACE_RIGHT") || token.equals("FR") || token.equals("TURN_RIGHT") || token.equals("TR") || token.equals("6")) {
                                moveRoute[i] = 6;
                            } else if (token.equals("LOOK_UP") || token.equals("LU") || token.equals("FACE_UP") || token.equals("FU") || token.equals("TURN_UP") || token.equals("TU") || token.equals("7")) {
                                moveRoute[i] = 7;
                            } else {
                                System.err.println("Warning: Invalid direction in move route: " + token);
                                moveRoute[i] = 0; // Default to Down
                            }
                        }
                    }

                    int initialDirection = parseDirectionProperty(rectObj.getProperties(), "initial_direction", -1);
                    if (initialDirection == -1) {
                        initialDirection = parseDirectionProperty(rectObj.getProperties(), "direction", 0);
                    }

                    return new MapEvent(name, bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight())
                        .scriptId(scriptId)
                        .solid(isSolid)
                        .spriteSheetName(spriteSheet)
                        .isStatic(isStatic)
                        .characterIndex(characterIndex)
                        .initialDirection(initialDirection)
                        .baseSpeed(baseSpeed)
                        .moveRouteType(moveRouteType)
                        .moveRoute(moveRoute)
                        .stepDelay(moveDelay);

                case "EnemyMapEvent":
                    // Parse EnemyMapEvent properties
                    String enemyGroupId = rectObj.getProperties().get("enemy_group_id", String.class);
                    if (enemyGroupId == null) {
                        enemyGroupId = "default";
                    }

                    boolean isEnemySolid = getBooleanProperty(rectObj.getProperties(), "is_solid", true);

                    String enemyRawPath = rectObj.getProperties().get("sprite_sheet", String.class);
                    String enemySpriteSheet = "";
                    if (enemyRawPath != null) {
                        enemySpriteSheet = new java.io.File(enemyRawPath).getName();
                    }

                    boolean isEnemyStatic = getBooleanProperty(rectObj.getProperties(), "is_static", false); // default false so they animate/move!

                    int enemyCharacterIndex = getIntProperty(rectObj.getProperties(), "character_index", 0);

                    float enemyBaseSpeed = getFloatProperty(rectObj.getProperties(), "base_speed", 50f);

                    // Autonomous Movement Properties
                    String enemyRawMoveType = rectObj.getProperties().get("autonomous_move_type", String.class);
                    MapEvent.MoveRouteType enemyMoveRouteType = MapEvent.MoveRouteType.STATIC;
                    if (enemyRawMoveType != null) {
                        try {
                            enemyMoveRouteType = MapEvent.MoveRouteType.valueOf(enemyRawMoveType.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            System.err.println("Warning: Invalid autonomous_move_type: " + enemyRawMoveType);
                        }
                    }

                    float enemyMoveDelay = getFloatProperty(rectObj.getProperties(), "autonomous_move_delay", 2.0f);

                    String enemyRawRoute = rectObj.getProperties().get("autonomous_move_route", String.class);
                    int[] enemyMoveRoute = null;
                    if (enemyRawRoute != null && !enemyRawRoute.trim().isEmpty()) {
                        String[] tokens = enemyRawRoute.split(",");
                        enemyMoveRoute = new int[tokens.length];
                        for (int i = 0; i < tokens.length; i++) {
                            String token = tokens[i].trim().toUpperCase();
                            if (token.equals("DOWN") || token.equals("0") || token.equals("D")) {
                                enemyMoveRoute[i] = 0;
                            } else if (token.equals("LEFT") || token.equals("1") || token.equals("L")) {
                                enemyMoveRoute[i] = 1;
                            } else if (token.equals("RIGHT") || token.equals("2") || token.equals("R")) {
                                enemyMoveRoute[i] = 2;
                            } else if (token.equals("UP") || token.equals("3") || token.equals("U")) {
                                enemyMoveRoute[i] = 3;
                            } else if (token.equals("LOOK_DOWN") || token.equals("LD") || token.equals("FACE_DOWN") || token.equals("FD") || token.equals("TURN_DOWN") || token.equals("TD") || token.equals("4")) {
                                enemyMoveRoute[i] = 4;
                            } else if (token.equals("LOOK_LEFT") || token.equals("LL") || token.equals("FACE_LEFT") || token.equals("FL") || token.equals("TURN_LEFT") || token.equals("TL") || token.equals("5")) {
                                enemyMoveRoute[i] = 5;
                            } else if (token.equals("LOOK_RIGHT") || token.equals("LR") || token.equals("FACE_RIGHT") || token.equals("FR") || token.equals("TURN_RIGHT") || token.equals("TR") || token.equals("6")) {
                                enemyMoveRoute[i] = 6;
                            } else if (token.equals("LOOK_UP") || token.equals("LU") || token.equals("FACE_UP") || token.equals("FU") || token.equals("TURN_UP") || token.equals("TU") || token.equals("7")) {
                                enemyMoveRoute[i] = 7;
                            } else {
                                System.err.println("Warning: Invalid direction in move route: " + token);
                                enemyMoveRoute[i] = 0; // Default to Down
                            }
                        }
                    }

                    int enemyInitialDirection = parseDirectionProperty(rectObj.getProperties(), "initial_direction", -1);
                    if (enemyInitialDirection == -1) {
                        enemyInitialDirection = parseDirectionProperty(rectObj.getProperties(), "direction", 0);
                    }

                    EnemyMapEvent enemyEvent = new EnemyMapEvent(name, bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), enemyGroupId);
                    enemyEvent.solid(isEnemySolid)
                        .spriteSheetName(enemySpriteSheet)
                        .isStatic(isEnemyStatic)
                        .characterIndex(enemyCharacterIndex)
                        .initialDirection(enemyInitialDirection)
                        .baseSpeed(enemyBaseSpeed)
                        .moveRouteType(enemyMoveRouteType)
                        .moveRoute(enemyMoveRoute)
                        .stepDelay(enemyMoveDelay);
                    return enemyEvent;

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

    private static float getFloatProperty(com.badlogic.gdx.maps.MapProperties properties, String key, float defaultValue) {
        Object value = properties.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        try {
            return Float.parseFloat(value.toString().replace(',', '.'));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static int getIntProperty(com.badlogic.gdx.maps.MapProperties properties, String key, int defaultValue) {
        Object value = properties.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static boolean getBooleanProperty(com.badlogic.gdx.maps.MapProperties properties, String key, boolean defaultValue) {
        Object value = properties.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }

    private static int parseDirectionProperty(com.badlogic.gdx.maps.MapProperties properties, String key, int defaultValue) {
        Object value = properties.get(key);
        if (value == null) {
            return defaultValue;
        }
        String strVal = value.toString().trim().toUpperCase();
        if (strVal.equals("DOWN") || strVal.equals("0") || strVal.equals("D")) {
            return 0;
        } else if (strVal.equals("LEFT") || strVal.equals("1") || strVal.equals("L")) {
            return 1;
        } else if (strVal.equals("RIGHT") || strVal.equals("2") || strVal.equals("R")) {
            return 2;
        } else if (strVal.equals("UP") || strVal.equals("3") || strVal.equals("U")) {
            return 3;
        }
        return defaultValue;
    }
}
