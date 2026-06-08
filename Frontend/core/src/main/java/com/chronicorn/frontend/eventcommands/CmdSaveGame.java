package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.managers.SaveManager;

public class CmdSaveGame implements EventCommand {
    private boolean isDone = false;

    @Override
    public void start() {
        SaveManager.getInstance().saveGame();
        isDone = true;
    }

    @Override
    public void update(float delta) {
        // Instant execution, no update logic needed
    }

    @Override
    public boolean isFinished() {
        return isDone;
    }
}
