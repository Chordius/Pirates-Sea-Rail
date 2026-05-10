package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.managers.eventManagers.GameMessage;

public class CmdShowText implements EventCommand {
    private String text;
    private String speaker = null;

    public CmdShowText(String text) {
        this.text = text;
        GameMessage.getInstance().setPosition(false);
    }

    public CmdShowText(String text, float x, float y) {
        this.text = text;
        GameMessage.getInstance().setPosition(true, x, y);
    }

    public CmdShowText(String speaker, String text) {
        this.speaker = speaker;
        this.text = text;
        GameMessage.getInstance().setPosition(false);
    }

    public CmdShowText setSpeaker(String speaker) {
        this.speaker = speaker;
        return this;
    }

    public CmdShowText setAlignment(int alignment) {
        GameMessage.getInstance().setAlignment(alignment);
        return this;
    }

    @Override
    public void start() {
        // Just dump the data into the global bucket
        if (speaker != null && !speaker.isEmpty()) {
            GameMessage.getInstance().setText(speaker, text);
        } else {
            GameMessage.getInstance().setText(text);
        }
    }

    @Override
    public void update(float delta) {
        // Do nothing. We are waiting for the UI to handle it.
    }

    @Override
    public boolean isFinished() {
        // We are finished ONLY when GameMessage says the window is closed
        return !GameMessage.getInstance().isBusy();
    }
}
