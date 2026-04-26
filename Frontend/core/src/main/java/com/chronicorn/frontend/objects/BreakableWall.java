package com.chronicorn.frontend.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.managers.SoundManager; // Import SoundManager
import com.chronicorn.frontend.managers.eventManagers.EventManager;

public class BreakableWall extends InteractiveObject {

    private static Texture wallTexture;
    private static TextureRegion wallRegion;
    private boolean isBroken = false;

    public static void loadAssets() {
        if (wallTexture == null) {
            wallTexture = new Texture(Gdx.files.internal("BreakableWall.png"));
            wallRegion = new TextureRegion(wallTexture, 0, 0, 96, 96);
        }
    }

    public BreakableWall(String name, float x, float y) {
        super(name, x, y, 96, 96);
        if (wallTexture == null) loadAssets();
    }

    @Override
    public void interact(Player player, EventManager events) {
        if (isBroken) return;

        // Cek apakah player sedang Dash
        if (player.isDashing()) {
            breakObject(events);
        }
    }

    private void breakObject(EventManager events) {
        isBroken = true;

        // --- TAMBAHAN SUARA ---
        // Pastikan ada file 'wall_break.wav' di folder sfx
        SoundManager.getInstance().playSound("wall_break1.wav");
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!isBroken) {
            batch.draw(wallRegion, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    @Override
    public boolean isSolid() {
        return !isBroken;
    }
}
