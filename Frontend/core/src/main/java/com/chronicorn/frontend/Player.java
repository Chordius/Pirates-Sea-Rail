package com.chronicorn.frontend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.chronicorn.frontend.contants.Direction;
import com.chronicorn.frontend.managers.SoundManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.objects.PhysicsObjects;
import com.chronicorn.frontend.states.NormalState;
import com.chronicorn.frontend.states.PlayerState;
import com.badlogic.gdx.utils.Array;
import com.chronicorn.frontend.observers.PlayerObserver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Player implements PhysicsObjects {
    // --- PHYSICS ---
    private Vector2 position;
    private Vector2 velocity;
    private float acceleration = 3000;
    private float friction = 3000f;
    private float maxSpeed = 300f;

    // --- STATE & COOLDOWN ---
    private PlayerState currentState;

    private Rectangle collider;

    // [FIX 1] Adjusted height to 64f to match the TextureRegion slice!
    private float width = 48f;
    private float height = 64f;
    private Vector2 respawnPosition;

    private Direction currentDirection = Direction.DOWN;

    // --- CONDITION FLAGS ---
    public boolean isBusy = false; // Used to freeze player during events
    private boolean transparent = false;

    // --- OVERWORLD STRIKE (NEW) ---
    private boolean isStriking = false;
    private float strikeTimer = 0f;
    private final float STRIKE_DURATION = 0.2f;

    public void recoverAll() {
        // Overworld character has no stats to recover
    }

    // --- SCORE ---
    private int score = 0;

    // --- AUDIO VARIABLES ---
    private long walkSoundId = -1;
    private String walkSoundFile = "horse_walk1.wav";

    // --- ANIMATION VARS ---
    Map<Direction, Animation<TextureRegion>> walkAnimations = new HashMap<>();
    Map<Direction, TextureRegion> idleFrames = new HashMap<>();
    Map<Direction, Animation<TextureRegion>> runAnimations = new HashMap<>();
    private TextureRegion currentFrame;
    private Texture textureSheet;
    private Texture runTextureSheet;
    private float stateTime;

    // --- CUTSCENE MOVEMENT ---
    private Vector2 cutsceneTargetPosition = new Vector2();
    private boolean isCutsceneMoving = false;
    private float cutsceneMoveSpeed = 70f;

    public void takeHazardDamage(int amount) {
        // TODO: Make Party Take Hazard DMG
    }

    public boolean isDashing() {
        return false;
    }

    public Player(Vector2 startPosition) {
        this.position = new Vector2(startPosition);
        this.velocity = new Vector2(0, 0);

        // Push the collider down to the feet for pseudo-3D collision
        this.collider = new Rectangle(position.x, position.y, width - 16, height / 3f);

        initializeAnimations();
        this.currentState = new NormalState();
        this.respawnPosition = new Vector2(startPosition);
    }

    // --- OVERWORLD STRIKE LOGIC ---

    // Call this from LevelMapManager when 'Z' is pressed!
    public void playSwingAnimation() {
        if (!isStriking) {
            this.isStriking = true;
            this.strikeTimer = STRIKE_DURATION;

            // You can add a sword swing sound effect here!
            // SoundManager.getInstance().playSoundWithCooldown("swing.wav", 0.2f);
        }
    }

    public void spawnGhost() {
        // Overworld character has no ghost trail
    }

    // --- STATE MANAGEMENT ---
    public void changeState(PlayerState newState) {
        this.currentState = newState;
    }

    public void handleInput() {
        if (isBusy) return; // Prevent moving while talking to NPCs
        currentState.handleInput(this);
    }

    public Vector2 getVelocity() { return velocity; }
    public float getMaxSpeed() { return maxSpeed; }

    // --- MAIN UPDATE LOOP ---
    public void update(float delta) {
        // Handle the visual strike cooldown
        if (isStriking) {
            strikeTimer -= delta;
            if (strikeTimer <= 0) isStriking = false;
        }

        if (isBusy && isCutsceneMoving) {
            // Smoothly interpolate player position to target during cutscene
            float dx = cutsceneTargetPosition.x - position.x;
            float dy = cutsceneTargetPosition.y - position.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance < cutsceneMoveSpeed * delta) {
                position.set(cutsceneTargetPosition);
                isCutsceneMoving = false;
            } else {
                position.x += (dx / distance) * cutsceneMoveSpeed * delta;
                position.y += (dy / distance) * cutsceneMoveSpeed * delta;
            }
            updateCollider();
        } else if (!isBusy) {
            currentState.update(this, delta);
            if (currentState instanceof NormalState) {
                currentState.handleInput(this);
            }
            position.mulAdd(velocity, delta);
            handleWalkingSound();
        }

        if (isBusy && !isCutsceneMoving) {
            stopWalkingSound();
            velocity.set(0,0); // Force stop sliding when an event starts
        }

        updateAnimation(delta);
        updateCollider();
    }

    // --- AUDIO LOGIC ---
    private void handleWalkingSound() {
        boolean isMoving = isMoving();
        if (isMoving && walkSoundId == -1) {
            walkSoundId = SoundManager.getInstance().loopSound(walkSoundFile);
        }
        else if (!isMoving && walkSoundId != -1) {
            stopWalkingSound();
        }
    }

    private void stopWalkingSound() {
        if (walkSoundId != -1) {
            SoundManager.getInstance().stopSound(walkSoundFile, walkSoundId);
            walkSoundId = -1;
        }
    }

    // --- PHYSICS HELPERS ---
    public void applyMovementForce(float dx, float dy) {
        velocity.x += acceleration * dx * Gdx.graphics.getDeltaTime();
        velocity.y += acceleration * dy * Gdx.graphics.getDeltaTime();
    }

    public void applyFriction(float delta) {
        float currentSpeed = velocity.len();
        if (currentSpeed > 0) {
            float newSpeed = currentSpeed - (friction * delta);
            if (newSpeed < 0) newSpeed = 0;
            velocity.setLength(newSpeed);
        }
    }

    // --- OBSERVER ---
    private List<PlayerObserver> observers = new ArrayList<>();

    public void addObserver(PlayerObserver observer) {
        this.observers.add(observer);
    }

    public void removeObserver(PlayerObserver observer) {
        this.observers.remove(observer);
    }

    private void notifyStatusObservers() {
    }

    private void notifyDashObservers() {
    }

    private void notifyHealthObservers() {
    }

    public void limitSpeed() {
        velocity.clamp(0, maxSpeed);
    }

    // --- GETTERS & SETTERS ---
    public Direction getCurrentDirection() { return currentDirection; }
    public void setCurrentDirection(Direction dir) { this.currentDirection = dir; }
    public boolean isDead() { return false; }
    public boolean isMoving() { return (velocity.len() > 10f) || isCutsceneMoving; }
    public boolean isTransparent() { return transparent; }
    public void setTransparent(boolean transparent) { this.transparent = transparent; }

    public boolean isCutsceneMoving() { return isCutsceneMoving; }

    public void setBaseSpeed(float speed) {
        this.cutsceneMoveSpeed = speed;
        this.maxSpeed = speed * 3f;
    }

    public void moveGrid(int direction, float tileSize) {
        changeState(new NormalState());
        this.cutsceneTargetPosition.set(position.x, position.y);
        switch (direction) {
            case 0:
                this.currentDirection = Direction.DOWN;
                this.cutsceneTargetPosition.y -= tileSize;
                break;
            case 1:
                this.currentDirection = Direction.LEFT;
                this.cutsceneTargetPosition.x -= tileSize;
                break;
            case 2:
                this.currentDirection = Direction.RIGHT;
                this.cutsceneTargetPosition.x += tileSize;
                break;
            case 3:
                this.currentDirection = Direction.UP;
                this.cutsceneTargetPosition.y += tileSize;
                break;
        }
        this.isCutsceneMoving = true;
    }

    public void turn(int direction) {
        changeState(new NormalState());
        switch (direction) {
            case 0: this.currentDirection = Direction.DOWN; break;
            case 1: this.currentDirection = Direction.LEFT; break;
            case 2: this.currentDirection = Direction.RIGHT; break;
            case 3: this.currentDirection = Direction.UP; break;
        }
    }

    // --- ANIMATION ---
    private void initializeAnimations() {
        // 1. Load Walk Sheet
        textureSheet = new Texture(Gdx.files.internal("feschar_big_040.png"));
        TextureRegion[][] tmpFrames = TextureRegion.split(textureSheet, 48, 64);

        // 2. Load Run Sheet
        runTextureSheet = new Texture(Gdx.files.internal("Hero01_dash.png"));
        int runFrameWidth = runTextureSheet.getWidth() / 3;
        int runFrameHeight = runTextureSheet.getHeight() / 4;
        TextureRegion[][] tmpRunFrames = TextureRegion.split(runTextureSheet, runFrameWidth, runFrameHeight);

        int[] rowMap = new int[4];
        for (int i = 0; i < Direction.values().length; i++) {
            Direction dir = Direction.values()[i];
            switch(dir) {
                case DOWN:  rowMap[i] = 0; break;
                case LEFT:  rowMap[i] = 1; break;
                case RIGHT: rowMap[i] = 2; break;
                case UP:    rowMap[i] = 3; break;
            }
        }

        for (int i = 0; i < Direction.values().length; i++) {
            Direction dir = Direction.values()[i];
            int rowInImage = rowMap[i];

            // Build Walk Animation
            TextureRegion[] frames = new TextureRegion[4];
            frames[0] = tmpFrames[rowInImage][1];
            frames[1] = tmpFrames[rowInImage][0];
            frames[2] = tmpFrames[rowInImage][1];
            frames[3] = tmpFrames[rowInImage][2];
            walkAnimations.put(dir, new Animation<>(1f/6f, frames));
            idleFrames.put(dir, tmpFrames[rowInImage][1]);

            // Build Run Animation
            TextureRegion[] rFrames = new TextureRegion[4];
            rFrames[0] = tmpRunFrames[rowInImage][1];
            rFrames[1] = tmpRunFrames[rowInImage][0];
            rFrames[2] = tmpRunFrames[rowInImage][1];
            rFrames[3] = tmpRunFrames[rowInImage][2];
            // 1f/10f makes the animation play faster to match the run speed
            runAnimations.put(dir, new Animation<>(1f/10f, rFrames));
        }
    }

    private void updateAnimation(float delta) {
        if (!isMoving()) {
            stateTime = 0f;
        } else {
            stateTime += delta;
        }

        if (isMoving()) {
            if (currentState instanceof com.chronicorn.frontend.states.DashingState) {
                currentFrame = runAnimations.get(currentDirection).getKeyFrame(stateTime, true);
            } else {
                currentFrame = walkAnimations.get(currentDirection).getKeyFrame(stateTime, true);
            }
        } else {
            currentFrame = idleFrames.get(currentDirection);
        }
    }

    public void transitionToWalkSpeed() {
        if (velocity.len() > maxSpeed) {
            velocity.setLength(maxSpeed);
        }
    }

    // --- RENDER ---
    public void render(SpriteBatch batch) {
        if (transparent) return;

        if (currentFrame != null) {
            float drawX = position.x;
            float drawY = position.y;

            float frameWidth = currentFrame.getRegionWidth();
            float frameHeight = currentFrame.getRegionHeight();

            float offsetX = (frameWidth - this.width) / 2f;
            float offsetY = 0f;

            // Simple visual cue for striking on the overworld
            if (isStriking) {
                batch.setColor(1f, 0.8f, 0.8f, 1f); // Slight red flash

                // Slight lunge visual effect
                switch(currentDirection) {
                    case UP: drawY += 5; break;
                    case DOWN: drawY -= 5; break;
                    case LEFT: drawX -= 5; break;
                    case RIGHT: drawX += 5; break;
                }
            } else {
                batch.setColor(1f, 1f, 1f, 1f);
            }


            batch.draw(currentFrame, drawX - offsetX, drawY - offsetY, frameWidth, frameHeight);
            batch.setColor(1f, 1f, 1f, 1f); // Reset color for the rest of the game
        }
    }

    public Vector2 getPosition() { return position; }
    public Rectangle getBounds() { return collider; }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
        updateCollider();
    }

    private void updateCollider() {
        // Keeps the collider at the player's feet
        collider.setPosition(position.x + 8, position.y);
    }

    public void dispose() {
        textureSheet.dispose();
        if (runTextureSheet != null) {
            runTextureSheet.dispose();
        }
    }

    public void setSpawnPoint(float x, float y) {
        this.setPosition(x, y);
        if (this.respawnPosition == null) {
            this.respawnPosition = new Vector2(x, y);
        } else {
            this.respawnPosition.set(x, y);
        }
        this.velocity.set(0, 0);
    }
}
