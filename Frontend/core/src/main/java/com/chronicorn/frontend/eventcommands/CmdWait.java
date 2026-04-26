package com.chronicorn.frontend.eventcommands;

public class CmdWait implements EventCommand {
    private float timer;
    private float duration;

    public CmdWait(float seconds) {
        this.duration = seconds;
    }

    @Override
    public void start() {
        timer = 0;
    }

    @Override
    public void update(float delta) {
        timer += delta;
    }

    @Override
    public boolean isFinished() {
        return timer >= duration;
    }
}
