package com.chronicorn.frontend.managers.eventManagers;

import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.eventcommands.EventCommand;
import com.chronicorn.frontend.windows.GabWindow;
import com.chronicorn.frontend.windows.WindowMessage;

public class EventManager {
    private Array<EventCommand> commandQueue;
    private EventCommand currentCommand;
    private boolean isProcessing;
    private WindowMessage messageWindow;
    private GabWindow flexWindow;

    public EventManager() {
        this.commandQueue = new Array<>();
        this.isProcessing = false;
    }

    public void setUI(WindowMessage window, GabWindow flex) {
        this.messageWindow = window;
        this.flexWindow = flex;
    }

    public void queue(EventCommand cmd) {
        commandQueue.add(cmd);
        isProcessing = true;
    }

    /**
     * Clears all events (useful for skip cutscene).
     */
    public void clear() {
        commandQueue.clear();
        currentCommand = null;
        isProcessing = false;
    }

    public void update(float delta) {
        if (!isProcessing) return;

        // 1. If we have no active command, try to get the next one
        if (currentCommand == null) {
            if (commandQueue.size > 0) {
                currentCommand = commandQueue.removeIndex(0);
                currentCommand.start();
            } else {
                isProcessing = false; // Queue is empty
                return;
            }
        }

        // 2. Update the active command
        currentCommand.update(delta);

        // 3. Check if done
        if (currentCommand.isFinished()) {
            currentCommand = null; // Ready for next frame to pick up the next command
        }
    }

    public boolean isBusy() {
        return isProcessing;
    }
}
