package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.managers.eventManagers.GameSession;

public class CmdFlagSet implements EventCommand {
    private final String flagName;
    private final boolean value;

    public CmdFlagSet(String flagName, boolean value) {
        this.flagName = flagName;
        this.value = value;
    }

    @Override
    public void start() {
        if (value) GameSession.getInstance().set(flagName); // Or set value depending on your GameSession implementation
        else GameSession.getInstance().unset(flagName);
    }

    @Override
    public void update(float delta) {}

    @Override
    public boolean isFinished() {
        return true;
    }
}
