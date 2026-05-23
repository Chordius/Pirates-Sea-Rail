package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.managers.eventManagers.GameSession;

public class CmdAddItem implements EventCommand {
    private final String itemId;
    private final int amount;
    private boolean isDone = false;

    public CmdAddItem(String itemId, int amount) {
        this.itemId = itemId;
        this.amount = amount;
    }

    @Override
    public void start() {
        GameSession.getInstance().inventory.addItem(itemId, amount);
        isDone = true;
    }

    @Override
    public void update(float delta) {
        // Nothing to update, completed in start()
    }

    @Override
    public boolean isFinished() {
        return isDone;
    }
}
