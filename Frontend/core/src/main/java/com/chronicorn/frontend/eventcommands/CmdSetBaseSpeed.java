package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.objects.InteractiveObject;
import com.chronicorn.frontend.objects.MapEvent;

public class CmdSetBaseSpeed implements EventCommand {
    private String entityName;
    private float speed;

    /**
     * Creates a new command to change the movement speed of a map event or player.
     * @param entityName The name of the entity to target (e.g. "Player" or map event name).
     * @param speed The new base speed.
     */
    public CmdSetBaseSpeed(String entityName, float speed) {
        this.entityName = entityName;
        this.speed = speed;
    }

    @Override
    public void start() {
        if (entityName.equalsIgnoreCase("Player")) {
            Player player = LevelMapManager.getInstance().getPlayer();
            if (player != null) {
                player.setBaseSpeed(speed);
            }
        } else {
            InteractiveObject obj = LevelMapManager.getInstance().getObjectByName(entityName);
            if (obj instanceof MapEvent) {
                ((MapEvent) obj).baseSpeed(speed);
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
