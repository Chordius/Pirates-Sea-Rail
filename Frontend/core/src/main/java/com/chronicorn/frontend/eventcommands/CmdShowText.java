package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.managers.eventManagers.GameMessage;

public class CmdShowText implements EventCommand {
    private String text;
    private String speaker = null;

    private boolean showBackground = true;
    private int alignment = com.badlogic.gdx.utils.Align.top;
    private boolean changePosition = false;
    private float x;
    private float y;

    public CmdShowText(String text) {
        this.text = text;
        this.changePosition = false;
    }

    public CmdShowText(String text, float x, float y) {
        this.text = text;
        this.changePosition = true;
        this.x = x;
        this.y = y;
    }

    public CmdShowText(String speaker, String text) {
        this.speaker = speaker;
        this.text = text;
        this.changePosition = false;
    }

    public CmdShowText setSpeaker(String speaker) {
        this.speaker = speaker;
        return this;
    }

    public CmdShowText setAlignment(int alignment) {
        this.alignment = alignment;
        return this;
    }

    public CmdShowText showBackground(boolean showBackground) {
        this.showBackground = showBackground;
        return this;
    }

    public CmdShowText setX(float x) {
        this.x = x;
        this.changePosition = true;
        return this;
    }

    public CmdShowText setY(float y) {
        this.y = y;
        this.changePosition = true;
        return this;
    }

    public CmdShowText setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        this.changePosition = true;
        return this;
    }

    public CmdShowText setChangePosition(boolean changePosition) {
        this.changePosition = changePosition;
        return this;
    }

    @Override
    public void start() {
        // Apply configurations to the GameMessage singleton at execution time
        GameMessage.getInstance().setAlignment(this.alignment);
        GameMessage.getInstance().setShowBackground(this.showBackground);
        if (changePosition) {
            GameMessage.getInstance().setPosition(true, this.x, this.y);
        } else {
            GameMessage.getInstance().setPosition(false);
        }

        // Dump the text data into the global bucket
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
