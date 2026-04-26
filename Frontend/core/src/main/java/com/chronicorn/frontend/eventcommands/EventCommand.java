package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.screens.MapScreen;

public interface EventCommand {
    /**
     * Called once when the command starts.
     */
    void start();

    /**
     * Called every frame.
     * @param delta Time since last frame
     */
    void update(float delta);

    /**
     * Returns true if this command is done and we can move to the next one.
     */
    boolean isFinished();
}
