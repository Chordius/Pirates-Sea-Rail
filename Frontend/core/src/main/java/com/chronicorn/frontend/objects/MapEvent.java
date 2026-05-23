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
    private float visualWidth = 48f;
    private float visualHeight = 64f;
    private int characterIndex;
    private String spriteSheetName = "";

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
