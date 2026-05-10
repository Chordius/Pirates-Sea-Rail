package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.objects.InteractiveObject;
import com.chronicorn.frontend.objects.MapEvent;

public class CmdMoveEntity implements EventCommand {

    private String entityName;
    private int direction; // 0=Down, 1=Left, 2=Right, 3=Up
    private float amountInTiles;
    private boolean isWait;

    private MapEvent targetEvent = null;
    private final float TILE_SIZE = 48f; // Standardize your grid size

    public CmdMoveEntity(String entityName, int direction, float amountInTiles, boolean isWait) {
        this.entityName = entityName;
        this.direction = direction;
        this.amountInTiles = amountInTiles;
        this.isWait = isWait;
    }

    @Override
    public void start() {
        InteractiveObject obj = LevelMapManager.getInstance().getObjectByName(entityName);

        if (obj instanceof MapEvent) {
            targetEvent = (MapEvent) obj;

            // 2. Tell the event to start sliding
            targetEvent.moveGrid(direction, amountInTiles * TILE_SIZE);
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
        // If we couldn't find the event, skip this command
        if (targetEvent == null) return true;

        // If isWait is true, the EventManager pauses here until the NPC arrives at the tile
        if (isWait) {
            return !targetEvent.isMoving;
        }

        // If isWait is false, the EventManager instantly goes to the next command while the NPC walks
        return true;
    }
}
