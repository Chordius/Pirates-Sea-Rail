package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.objects.InteractiveObject;
import com.chronicorn.frontend.objects.MapEvent;

public class CmdMoveEntity implements EventCommand {

    private String entityName;
    private int direction; // 0=Down, 1=Left, 2=Right, 3=Up
    private float amountInTiles;
    private boolean isWait;

    private MapEvent targetEvent = null;
    private Player targetPlayer = null;
    private final float TILE_SIZE = 48f; // Standardize your grid size

    public CmdMoveEntity(String entityName, int direction, float amountInTiles, boolean isWait) {
        this.entityName = entityName;
        this.direction = direction;
        this.amountInTiles = amountInTiles;
        this.isWait = isWait;
    }

    public CmdMoveEntity(String entityName, String directionStr, float amountInTiles, boolean isWait) {
        this.entityName = entityName;
        this.direction = parseDirectionString(directionStr);
        this.amountInTiles = amountInTiles;
        this.isWait = isWait;
    }

    private int parseDirectionString(String directionStr) {
        if (directionStr == null)
            return 0;
        String str = directionStr.trim().toUpperCase();
        switch (str) {
            case "DOWN":
            case "0":
            case "D":
                return 0;
            case "LEFT":
            case "1":
            case "L":
                return 1;
            case "RIGHT":
            case "2":
            case "R":
                return 2;
            case "UP":
            case "3":
            case "U":
                return 3;
            case "LOOK_DOWN":
            case "LD":
            case "FACE_DOWN":
            case "FD":
            case "TURN_DOWN":
            case "TD":
            case "4":
                return 4;
            case "LOOK_LEFT":
            case "LL":
            case "FACE_LEFT":
            case "FL":
            case "TURN_LEFT":
            case "TL":
            case "5":
                return 5;
            case "LOOK_RIGHT":
            case "LR":
            case "FACE_RIGHT":
            case "FR":
            case "TURN_RIGHT":
            case "TR":
            case "6":
                return 6;
            case "LOOK_UP":
            case "LU":
            case "FACE_UP":
            case "FU":
            case "TURN_UP":
            case "TU":
            case "7":
                return 7;
            default:
                try {
                    return Integer.parseInt(str);
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Invalid direction string: " + directionStr);
                    return 0; // Default to Down
                }
        }
    }

    @Override
    public void start() {
        if (entityName.equalsIgnoreCase("Player")) {
            targetPlayer = LevelMapManager.getInstance().getPlayer();
            if (targetPlayer != null) {
                if (direction >= 4 && direction <= 7) {
                    targetPlayer.turn(direction - 4);
                } else {
                    targetPlayer.moveGrid(direction, amountInTiles * TILE_SIZE);
                }
            }
        } else {
            InteractiveObject obj = LevelMapManager.getInstance().getObjectByName(entityName);
            if (obj instanceof MapEvent) {
                targetEvent = (MapEvent) obj;
                if (direction >= 4 && direction <= 7) {
                    targetEvent.turn(direction - 4);
                } else {
                    targetEvent.moveGrid(direction, amountInTiles * TILE_SIZE);
                }
            }
        }
    }

    @Override
    public void update(float delta) {
        if (targetEvent != null && isWait) {
            // In wait-mode this command owns the movement tick.
            targetEvent.update(delta);
        }
    }

    @Override
    public boolean isFinished() {
        if (targetPlayer != null) {
            if (isWait) {
                return !targetPlayer.isCutsceneMoving();
            }
            return true;
        }

        if (targetEvent == null)
            return true;

        if (isWait) {
            return !targetEvent.isMoving;
        }

        return true;
    }
}
