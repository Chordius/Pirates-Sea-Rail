package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.managers.ResetManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;

public class CmdDecreaseCountdown implements EventCommand {
    private boolean isFinished = false;

    @Override
    public void start() {
        int current = GameSession.getInstance().getInt("ROOM_COUNTDOWN");

        if (current > 0) {
            current--;
            GameSession.getInstance().setVar("ROOM_COUNTDOWN", current);
            System.out.println("Moves Remaining: " + current);
        }

        System.out.println("Current Room Count: " + GameSession.getInstance().getInt("ROOM_COUNTDOWN"));

        isFinished = true;
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }
}
