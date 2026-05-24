package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.objects.InteractiveObject;
import com.chronicorn.frontend.objects.MapEvent;

public class CmdSetEventLocation implements EventCommand {
    private String entityName;
    private float tileX;
    private float tileY;
    private int direction = -1; // -1 means keep current direction

    /**
     * Set event location with specific tile coordinates and direction.
     * @param entityName Name of the MapEvent entity.
     * @param tileX Target tile column.
     * @param tileY Target tile row.
     * @param direction Target direction (0=Down, 1=Left, 2=Right, 3=Up).
     */
    public CmdSetEventLocation(String entityName, float tileX, float tileY, int direction) {
        this.entityName = entityName;
        this.tileX = tileX;
        this.tileY = tileY;
        this.direction = direction;
    }

    /**
     * Set event location with specific tile coordinates, keeping current direction.
     * @param entityName Name of the MapEvent entity.
     * @param tileX Target tile column.
     * @param tileY Target tile row.
     */
    public CmdSetEventLocation(String entityName, float tileX, float tileY) {
        this.entityName = entityName;
        this.tileX = tileX;
        this.tileY = tileY;
        this.direction = -1;
    }

    @Override
    public void start() {
        InteractiveObject obj = LevelMapManager.getInstance().getObjectByName(entityName);
        if (obj instanceof MapEvent) {
            if (direction >= 0) {
                ((MapEvent) obj).setLocation(tileX, tileY, direction);
            } else {
                ((MapEvent) obj).setLocation(tileX, tileY);
            }
        }
    }

    @Override
    public void update(float delta) {}

    @Override
    public boolean isFinished() {
        return true;
    }
}
