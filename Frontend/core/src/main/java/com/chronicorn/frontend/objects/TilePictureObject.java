package com.chronicorn.frontend.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;

public class TilePictureObject extends InteractiveObject {
    private TextureRegion texture;

    public TilePictureObject(String name, float x, float y, float width, float height, TextureRegion texture,
            boolean isSolid) {
        super(name, x, y, width, height);
        this.texture = texture;
        this.isSolid = isSolid;
    }

    @Override
    public void interact(Player player, EventManager events) {
        // Editable interactable: we can override/trigger custom scripts using the
        // object's name
        com.chronicorn.frontend.scripts.MapScript script = LevelMapManager.getInstance().getCurrentScript();
        if (script != null) {
            script.onTrigger(getName(), events);
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!isConditionMet())
            return;
        if (texture != null) {
            batch.draw(texture, x, y, width, height);
        }
    }
}
