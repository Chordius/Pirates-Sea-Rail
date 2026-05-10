package com.chronicorn.frontend.managers.eventManagers;

import com.badlogic.gdx.utils.Align;

public class GameMessage {
    private static final GameMessage instance = new GameMessage();
    private String textToDisplay = null;
    private String speakerToDisplay = null;
    private boolean isBusy = false;
    private float x;
    private float y;
    private boolean changePosition = false;
    private int alignment = Align.top;

    public static GameMessage getInstance() {
        return instance;
    }

    public void setText(String speaker, String text) {
        this.speakerToDisplay = speaker;
        this.textToDisplay = text;
        this.isBusy = true;
        this.alignment = Align.top;
    }

    public void setText(String text) {
        this.textToDisplay = text;
        this.isBusy = true; // Mark system as busy so events pause
        this.alignment = Align.top;
    }

    public void setPosition(boolean bool, float x, float y) {
        this.x = x;
        this.y = y;
        changePosition = bool;
    }

    public void setPosition(boolean bool) {
        changePosition = bool;
    }

    public void setAlignment(int align) {
        alignment = align;
    }

    public String popSpeaker() {
        String s = speakerToDisplay;
        speakerToDisplay = null;
        return s;
    }

    public boolean getPosition() {
        return changePosition;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getAlignment() {
        return alignment;
    }

    public boolean hasText() {
        return textToDisplay != null;
    }

    public String popText() {
        String t = textToDisplay;
        textToDisplay = null; // Clear it so we don't read it twice
        return t;
    }

    public void finish() {
        this.isBusy = false; // Tell the EventManager it's safe to continue
    }

    public boolean isBusy() {
        return isBusy;
    }
}
