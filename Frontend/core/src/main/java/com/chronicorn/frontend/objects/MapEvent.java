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
    public enum MoveRouteType {
        STATIC,
        RANDOM,
        CUSTOM
    }

    private String scriptId = "";
    private boolean isStatic;

    private Map<Integer, Animation<TextureRegion>> walkAnimations;
    private Map<Integer, TextureRegion> idleFrames;
    private float stateTime = 0f;
    private int currentDirection = 0;

    private Vector2 targetPosition;
    private float moveSpeed = 100f; // Floating point speed
    public boolean isMoving = false;
    private int characterIndex;
    private String spriteSheetName = "";
    private boolean steppingAnimation = false;
    private boolean fixedDirection = false;

    // Autonomous Movement Fields
    private MoveRouteType moveRouteType = MoveRouteType.STATIC;
    private int[] moveRoute = new int[]{};
    private int routeIndex = 0;
    private float stepTimer = 0f;
    private float stepDelay = 2.0f; // Seconds to wait between steps

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

    public MapEvent initialDirection(int direction) {
        this.currentDirection = direction;
        if (idleFrames != null && idleFrames.containsKey(direction)) {
            this.currentFrame = idleFrames.get(direction);
        }
        return this;
    }

    public MapEvent moveRouteType(MoveRouteType moveRouteType) {
        this.moveRouteType = moveRouteType;
        return this;
    }

    public MapEvent moveRoute(int[] moveRoute) {
        this.moveRoute = (moveRoute != null) ? moveRoute : new int[]{};
        this.routeIndex = 0;
        return this;
    }

    public MapEvent stepDelay(float stepDelay) {
        this.stepDelay = stepDelay;
        this.stepTimer = stepDelay; // Initialize timer
        return this;
    }

    public MapEvent steppingAnimation(boolean steppingAnimation) {
        this.steppingAnimation = steppingAnimation;
        return this;
    }

    public MapEvent fixedDirection(boolean fixedDirection) {
        this.fixedDirection = fixedDirection;
        return this;
    }

    public MapEvent visibilityFlag(String visibilityFlag) {
        this.visibilityFlag = visibilityFlag;
        return this;
    }

    public MapEvent visibilityFlagIs(boolean visibilityFlagIs) {
        this.visibilityFlagIs = visibilityFlagIs;
        return this;
    }

    public boolean isSteppingAnimation() {
        return steppingAnimation;
    }

    public void setSteppingAnimation(boolean steppingAnimation) {
        this.steppingAnimation = steppingAnimation;
    }

    public boolean isFixedDirection() {
        return fixedDirection;
    }

    public void setFixedDirection(boolean fixedDirection) {
        this.fixedDirection = fixedDirection;
    }

    public void changeSprite(String spriteSheetName, int characterIndex) {
        if (spriteSheetName != null) {
            this.spriteSheetName = new java.io.File(spriteSheetName).getName();
        } else {
            this.spriteSheetName = "";
        }
        this.characterIndex = characterIndex;
        initializeAnimations(this.spriteSheetName);
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

        this.currentFrame = idleFrames.get(currentDirection);
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
        } else {
            // Autonomous movement check when not moving
            boolean isGameBusy = LevelMapManager.getInstance().getEventManager() != null && LevelMapManager.getInstance().getEventManager().isBusy();
            if (!isGameBusy && moveRouteType != MoveRouteType.STATIC) {
                stepTimer -= delta;
                if (stepTimer <= 0) {
                    takeAutonomousStep();
                    stepTimer = stepDelay; // Reset step delay
                }
            }
            if (steppingAnimation) {
                stateTime += delta;
            }
        }

        // --- UPDATE ANIMATION EXACTLY LIKE PLAYER ---
        if (!isStatic && walkAnimations != null && !walkAnimations.isEmpty()) {
            if (isMoving || steppingAnimation) {
                currentFrame = walkAnimations.get(currentDirection).getKeyFrame(stateTime, true);
            } else {
                currentFrame = idleFrames.get(currentDirection);
            }
        }
    }

    private void takeAutonomousStep() {
        int nextDir = -1;
        if (moveRouteType == MoveRouteType.RANDOM) {
            nextDir = (int) (Math.random() * 4);
        } else if (moveRouteType == MoveRouteType.CUSTOM && moveRoute.length > 0) {
            nextDir = moveRoute[routeIndex];
        }

        if (nextDir != -1) {
            if (nextDir >= 4 && nextDir <= 7) {
                // Look/Turn command
                int lookDir = nextDir - 4;
                turn(lookDir);
                if (moveRouteType == MoveRouteType.CUSTOM) {
                    routeIndex = (routeIndex + 1) % moveRoute.length;
                }
                return;
            }

            float TILE_SIZE = 48f;
            float nextX = this.x;
            float nextY = this.y;
            switch (nextDir) {
                case 0: nextY -= TILE_SIZE; break; // Down
                case 1: nextX -= TILE_SIZE; break; // Left
                case 2: nextX += TILE_SIZE; break; // Right
                case 3: nextY += TILE_SIZE; break; // Up
            }

            // Check collision
            boolean blocked = LevelMapManager.getInstance().isAreaBlocked(nextX, nextY, this.width, this.height, this);
            if (!blocked) {
                moveGrid(nextDir, TILE_SIZE);
                if (moveRouteType == MoveRouteType.CUSTOM) {
                    routeIndex = (routeIndex + 1) % moveRoute.length;
                }
            } else {
                // Update direction visual even if blocked, so they face the wall they bumped into
                turn(nextDir);
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

    public void turn(int direction) {
        if (direction >= 0 && direction <= 3) {
            this.currentDirection = direction;
            if (idleFrames != null && idleFrames.containsKey(direction)) {
                this.currentFrame = idleFrames.get(direction);
            }
        }
    }

    public void setLocation(float tileX, float tileY) {
        float TILE_SIZE = 48f;
        float mapHeight = LevelMapManager.getInstance().getMapHeight();

        float pixelX = tileX * TILE_SIZE;
        float pixelY = (mapHeight - 1 - tileY) * TILE_SIZE;

        this.x = pixelX;
        this.y = pixelY;
        this.targetPosition.set(pixelX, pixelY);
        this.bounds.setPosition(pixelX, pixelY);
        this.isMoving = false;

        // Reset idle frame for current direction
        if (idleFrames != null && idleFrames.containsKey(currentDirection)) {
            this.currentFrame = idleFrames.get(currentDirection);
        }
    }

    public void setLocation(float tileX, float tileY, int direction) {
        setLocation(tileX, tileY);
        turn(direction);
    }

    @Override
    public void interact(Player player, EventManager events) {
        if (events.isBusy()) return;

        if (!fixedDirection) {
            switch(player.getCurrentDirection()) {
                case DOWN: this.currentDirection = 3; break;
                case LEFT: this.currentDirection = 2; break;
                case RIGHT: this.currentDirection = 1; break;
                case UP: this.currentDirection = 0; break;
            }
            if (idleFrames != null && idleFrames.containsKey(currentDirection)) {
                this.currentFrame = idleFrames.get(currentDirection);
            }
        }

        // Fetch the active script for the map the player is currently standing in
        MapScript currentScript = LevelMapManager.getInstance().getCurrentScript();

        if (currentScript != null) {
            // Treat the interaction exactly like a ghost trigger!
            currentScript.onTrigger(this.scriptId, events);
        }
    }

    @Override
    public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        if (!isConditionMet()) return;
        if (currentFrame != null) {
            float frameWidth = currentFrame.getRegionWidth();
            float frameHeight = currentFrame.getRegionHeight();

            // Center the sprite horizontally over the hitbox bounds
            float offsetX = (frameWidth - this.width) / 2f;
            // Draw bottom-aligned, so extra height extends upwards
            float offsetY = 0f;

            batch.draw(currentFrame, this.x - offsetX, this.y - offsetY, frameWidth, frameHeight);
        }
    }
}
