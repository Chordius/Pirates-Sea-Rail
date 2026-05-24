package com.chronicorn.frontend.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.managers.eventManagers.EventManager;

public abstract class InteractiveObject implements PhysicsObjects {
    protected float x, y;
    protected float width, height;
    protected Rectangle bounds;
    protected String name;
    protected TextureRegion currentFrame;
    protected boolean isSolid = true; // If true, player can't walk through it
    protected String visibilityFlag = null;
    protected boolean visibilityFlagIs = true;

    public InteractiveObject(String name, float x, float y, float width, float height) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public void setVisibilityFlag(String flag) {
        this.visibilityFlag = flag;
    }

    public String getVisibilityFlag() {
        return visibilityFlag;
    }

    public void setVisibilityFlagIs(boolean visibilityFlagIs) {
        this.visibilityFlagIs = visibilityFlagIs;
    }

    public boolean getVisibilityFlagIs() {
        return visibilityFlagIs;
    }

    public boolean isConditionMet() {
        if (visibilityFlag == null || visibilityFlag.trim().isEmpty()) {
            return true;
        }
        return com.chronicorn.frontend.managers.eventManagers.GameSession.getInstance().isSet(visibilityFlag) == visibilityFlagIs;
    }

    public void render(SpriteBatch batch) {
        if (!isConditionMet()) return;
        if (currentFrame != null) {
            batch.draw(currentFrame, x, y, width, height);
        }
    }

    public String getName() {
        return name;
    }
    public float getWidth() {
        return width;
    }
    public float getHeight() {
        return height;
    }
    public boolean isSolid() {
        return isSolid;
    }

    public abstract void interact(Player player, EventManager events);

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public Vector2 getPosition() {
        return new Vector2(x, y);
    }
}
