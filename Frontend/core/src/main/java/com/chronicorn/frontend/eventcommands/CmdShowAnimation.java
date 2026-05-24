package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.managers.animationManager.VFXActor;
import com.chronicorn.frontend.objects.PhysicsObjects;

public class CmdShowAnimation implements EventCommand {
    private String entityName;
    private String vfxId;
    private boolean isWait;

    private VFXActor spawnedVfx = null;
    private boolean started = false;

    /**
     * Creates a new event command to play a VFX animation on an overworld entity.
     * @param entityName Target entity name (e.g., "player" or the event name from Tiled).
     * @param vfxId The ID of the VFX from vfx_data.json, or fallback texture name.
     * @param isWait If true, the event queue will block until the animation finishes.
     */
    public CmdShowAnimation(String entityName, String vfxId, boolean isWait) {
        this.entityName = entityName;
        this.vfxId = vfxId;
        this.isWait = isWait;
    }

    @Override
    public void start() {
        PhysicsObjects targetEntity = null;
        if (entityName.equalsIgnoreCase("player")) {
            targetEntity = LevelMapManager.getInstance().getPlayer();
        } else {
            targetEntity = LevelMapManager.getInstance().getObjectByName(entityName);
        }

        if (targetEntity != null) {
            spawnedVfx = LevelMapManager.getInstance().showVFX(targetEntity, vfxId);
        }
        started = true;
    }

    @Override
    public void update(float delta) {
        // No per-frame polling state needed for the command itself
    }

    @Override
    public boolean isFinished() {
        if (!started) return false;
        if (isWait && spawnedVfx != null) {
            return spawnedVfx.isFinished();
        }
        return true;
    }
}
