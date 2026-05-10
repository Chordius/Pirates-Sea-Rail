package com.chronicorn.frontend.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.managers.SoundManager; // Import SoundManager
import com.chronicorn.frontend.managers.eventManagers.EventManager;

public class Vase extends InteractiveObject {

    private TextureRegion texture;
    private boolean isBroken = false;

    public Vase(String name, float x, float y, TextureRegion texture) {
        super(name, x, y, 48, 48);
        this.texture = texture;
    }

    @Override
    public void interact(Player player, EventManager events) {
        if (isBroken) return;
        if (player.isDashing()) {
            breakObject(events);
        }
    }

    private void breakObject(EventManager events) {
        isBroken = true;
        SoundManager.getInstance().playSound("wall_break2.wav");
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!isBroken) {
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    @Override
    public boolean isSolid() {
        return !isBroken;
    }
}
