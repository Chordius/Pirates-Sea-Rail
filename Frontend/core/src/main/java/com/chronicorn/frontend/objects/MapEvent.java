package com.chronicorn.frontend.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.managers.assetManager.ImageManager;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.scripts.MapScript;

import java.util.HashMap;
import java.util.Map;

public class MapEvent extends InteractiveObject {
    private String scriptId = "";
    private boolean isStatic;

    private Map<Integer, Animation<TextureRegion>> walkAnimations;
    private Map<Integer, TextureRegion> idleFrames;
    private float stateTime = 0f;
    private int currentDirection = 0;

    private Vector2 targetPosition;
    private float moveSpeed = 100f; // Floating point speed
    public boolean isMoving = false;
    private float visualWidth = 48f;
    private float visualHeight = 64f;
    private int characterIndex;
    private String spriteSheetName = "";

    public MapEvent(String name, float x, float y, float width, float height) {
        super(name, x, y, width, height);
        this.targetPosition = new Vector2(x, y);
        initializeAnimations(this.spriteSheetName);
    }

    public MapEvent scriptId(String scriptId) {
        this.scriptId = scriptId;
        return this;
    }

    public MapEvent solid(boolean isSolid) {
        this.isSolid = isSolid;
        return this;
    }

    public MapEvent spriteSheetName(String spriteSheetName) {
        this.spriteSheetName = (spriteSheetName != null) ? spriteSheetName : "";
        initializeAnimations(this.spriteSheetName);
        return this;
    }

    public MapEvent isStatic(boolean isStatic) {
        this.isStatic = isStatic;
        return this;
    }

    public MapEvent baseSpeed(float baseSpeed) {
        this.moveSpeed = baseSpeed;
        return this;
    }

    public MapEvent characterIndex(int characterIndex) {
        this.characterIndex = characterIndex;
        if (spriteSheetName != null && !spriteSheetName.isEmpty()) {
            initializeAnimations(spriteSheetName);
        }
        return this;
    }

    private void initializeAnimations(String spriteSheetName) {
        walkAnimations = new HashMap<>();
        idleFrames = new HashMap<>();

        if (spriteSheetName == null || spriteSheetName.isEmpty()) {
            this.currentFrame = null;
            return;
        }

        // Fetch the 12x8 grid
        TextureRegion[][] tmpFrames = ImageManager.getFullCharacterSheet(spriteSheetName);

        // Calculate the starting row and column for this specific character
        // index % 4 gets the column position (0 to 3). Multiply by 3 frames per character.
        int startCol = (characterIndex % 4) * 3;

        // index / 4 gets the row position (0 or 1). Multiply by 4 directions per character.
        int startRow = (characterIndex / 4) * 4;

        int[] rowMap = {0, 1, 2, 3}; // Down, Left, Right, Up (Relative to the startRow)

        for (int i = 0; i < rowMap.length; i++) {
            int rowInImage = startRow + rowMap[i];

            TextureRegion[] frames = new TextureRegion[4];

            // Map the specific columns starting from startCol
            frames[0] = tmpFrames[rowInImage][startCol + 1]; // Stand
            frames[1] = tmpFrames[rowInImage][startCol + 0]; // Step 1
            frames[2] = tmpFrames[rowInImage][startCol + 1]; // Stand
            frames[3] = tmpFrames[rowInImage][startCol + 2]; // Step 2

            walkAnimations.put(i, new Animation<>(1f/5f, frames));
            idleFrames.put(i, tmpFrames[rowInImage][startCol + 1]);
        }

        this.currentFrame = idleFrames.get(0);
    }

    public void update(float delta) {
        if (isMoving) {
            stateTime += delta;

            // Calculate distance to target (Floating point math)
            float dx = targetPosition.x - this.x;
            float dy = targetPosition.y - this.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // If close enough to destination, snap and stop
            if (distance < moveSpeed * delta) {
                this.x = targetPosition.x;
                this.y = targetPosition.y;
                this.isMoving = false;
                stateTime = 0f; // Reset animation timer
            } else {
                // Keep sliding smoothly using floats
                this.x += (dx / distance) * moveSpeed * delta;
                this.y += (dy / distance) * moveSpeed * delta;
            }

            this.bounds.setPosition(this.x, this.y); // Update physics bounds
        }

        // --- UPDATE ANIMATION EXACTLY LIKE PLAYER ---
        if (!isStatic && walkAnimations != null && !walkAnimations.isEmpty()) {
            if (isMoving) {
                currentFrame = walkAnimations.get(currentDirection).getKeyFrame(stateTime, true);
            } else {
                currentFrame = idleFrames.get(currentDirection);
            }
        }
    }

    // Called by CmdMoveEntity
    public void moveGrid(int direction, float tileSize) {
        this.currentDirection = direction;
        this.isMoving = true;

        switch (direction) {
            case 0: targetPosition.y -= tileSize; break; // Down
            case 1: targetPosition.x -= tileSize; break; // Left
            case 2: targetPosition.x += tileSize; break; // Right
            case 3: targetPosition.y += tileSize; break; // Up
        }
    }

    @Override
    public void interact(Player player, EventManager events) {
        if (events.isBusy()) return;

        switch(player.getCurrentDirection()) {
            case DOWN: this.currentDirection = 3; break;
            case LEFT: this.currentDirection = 2; break;
            case RIGHT: this.currentDirection = 1; break;
            case UP: this.currentDirection = 0; break;
        }
        this.currentFrame = idleFrames.get(currentDirection);

        // Fetch the active script for the map the player is currently standing in
        MapScript currentScript = LevelMapManager.getInstance().getCurrentScript();

        if (currentScript != null) {
            // Treat the interaction exactly like a ghost trigger!
            currentScript.onTrigger(this.scriptId, events);
        }
    }

    @Override
    public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        if (currentFrame != null) {

            // The physical box (this.width, this.height) from Tiled is 48x48.
            // We draw the texture using visualWidth (48) and visualHeight (64).
            // Because LibGDX draws from the bottom-left (this.x, this.y),
            // the extra 16 pixels will automatically stick out of the TOP of the hitbox!

            batch.draw(currentFrame, this.x, this.y, visualWidth, visualHeight);

            // NOTE: If you wanted a shadow or hit flash, you'd apply it here just like the Player class!
        }
    }
}
