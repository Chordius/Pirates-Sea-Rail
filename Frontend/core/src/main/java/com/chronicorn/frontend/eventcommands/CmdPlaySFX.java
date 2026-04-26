package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.managers.SoundManager;

public class CmdPlaySFX implements EventCommand {
    // Config
    private String fileName;
    private float volume = 1.0f;

    // Blocking Logic
    private float waitDuration = 0f; // 0 = Fire and forget
    private float timer = 0f;
    private boolean isFinished = false;

    public CmdPlaySFX(String fileName) {
        this.fileName = fileName;
    }

    public CmdPlaySFX setVolume(float volume) {
        this.volume = volume;
        return this;
    }

    public CmdPlaySFX waitForCompletion(float seconds) {
        this.waitDuration = seconds;
        return this;
    }

    @Override
    public void start() {
        // 1. Play the sound immediately
        SoundManager.getInstance().playSound(fileName, volume);

        // 2. Check if we need to wait
        if (waitDuration > 0) {
            timer = 0;
            isFinished = false;
        } else {
            isFinished = true; // Done immediately
        }
    }

    @Override
    public void update(float delta) {
        // Only runs if we set a waitDuration
        if (!isFinished) {
            timer += delta;
            if (timer >= waitDuration) {
                isFinished = true;
            }
        }
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }
}
