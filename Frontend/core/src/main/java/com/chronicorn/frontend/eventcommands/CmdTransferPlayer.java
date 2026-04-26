package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;

public class CmdTransferPlayer implements EventCommand {
    private String mapName;
    private float x = -1;
    private float y = -1;
    private boolean isDone = false;

    // We pass the dependencies in the constructor
    public CmdTransferPlayer(String mapName, float x, float y) {
        this.mapName = mapName;
        this.x = x;
        this.y = y;
    }

    public CmdTransferPlayer(String mapName) {
        this.mapName = mapName;
        this.x = -1;
        this.y = -1;
    }

    @Override
    public void start() {
        GameSession.getInstance().createSnapshot();

        if (x == -1 && y == -1) {
            LevelMapManager.getInstance().changeLevel(mapName);
        } else {
            LevelMapManager.getInstance().changeLevel(mapName, x, y);
        }

        // 3. Mark as done instantly
        isDone = true;
    }

    public CmdTransferPlayer setTargetX(float x) {
        this.x = x;
        return this;
    }

    public CmdTransferPlayer setTargetY(float y) {
        this.y = y;
        return this;
    }

    public CmdTransferPlayer setTargetY(String mapName) {
        this.mapName = mapName;
        return this;
    }

    @Override
    public void update(float delta) {
        // No update logic needed for instant transfer
    }

    @Override
    public boolean isFinished() {
        return isDone;
    }
}
