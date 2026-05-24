package com.chronicorn.frontend.eventcommands;

import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.objects.InteractiveObject;
import com.chronicorn.frontend.objects.MapEvent;

public class CmdSetEventSprite implements EventCommand {
    private String entityName;
    private String spriteSheetName;
    private int characterIndex;

    /**
     * Creates a command to change the sprite sheet and character index of a MapEvent.
     * @param entityName The name of the MapEvent entity to target.
     * @param spriteSheetName The new sprite sheet name (e.g. "Actor1.png" or "characters/Actor1.png").
     * @param characterIndex The index of the character within the 8-character sheet (0 to 7).
     */
    public CmdSetEventSprite(String entityName, String spriteSheetName, int characterIndex) {
        this.entityName = entityName;
        this.spriteSheetName = spriteSheetName;
        this.characterIndex = characterIndex;
    }

    @Override
    public void start() {
        InteractiveObject obj = LevelMapManager.getInstance().getObjectByName(entityName);
        if (obj instanceof MapEvent) {
            ((MapEvent) obj).changeSprite(spriteSheetName, characterIndex);
        }
    }

    @Override
    public void update(float delta) {}

    @Override
    public boolean isFinished() {
        return true;
    }
}
