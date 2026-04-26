package com.chronicorn.frontend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils; // Pastikan import ini ada
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.chronicorn.frontend.contants.Direction;
import com.chronicorn.frontend.managers.SoundManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.monsters.Monster;
import com.chronicorn.frontend.objects.PhysicsObjects;
import com.chronicorn.frontend.states.DashingState;
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
    private float dashCooldownTimer = 0;
    private float invincibilityTimer = 0;
    private final float DASH_COOLDOWN = 1.0f;
    private final float INVINCIBILITY_DURATION = 0.2f;

    private Rectangle collider;
    private float width = 64f;
    private float height = 64f;
    private Vector2 respawnPosition;

    private Direction currentDirection = Direction.UP;

    // --- CONDITION FLAGS ---
    public boolean dead = false;
    public boolean isBusy = false;
    public boolean isInvincible = false;

    // --- STATS ---
    public int hp = 35;
    private int maxHp = 35;
    private int atk = 35;
    private int def = 25;

    // --- SCORE ---
    private int score = 0;

    private final float BUMP_BASE_VALUE = 55f;

    private boolean isHit = false;
    private float hitTimer = 0;

    // --- AUDIO VARIABLES ---
    private long walkSoundId = -1;
    private String walkSoundFile = "horse_walk1.wav";

    // --- ANIMATION VARS ---
    Map<Direction, Animation<TextureRegion>> walkAnimations = new HashMap<>();
    Map<Direction, TextureRegion> idleFrames = new HashMap<>();
    private TextureRegion currentFrame;
    private Texture textureSheet;
    private float stateTime;

    // --- GHOST TRAIL ---
    private Array<Ghost> ghosts = new Array<>();

    private class Ghost {
        float x, y;
        TextureRegion texture;
        float alpha = 1.0f;
        float lifeTime = 0.5f;

        public Ghost(float x, float y, TextureRegion texture) {
            this.x = x;
            this.y = y;
            this.texture = texture;
        }
    }

    public Player(Vector2 startPosition) {
        this.position = new Vector2(startPosition);
        this.velocity = new Vector2(0, 0);
        this.collider = new Rectangle(position.x, position.y, width - 16, height - 16);
        initializeAnimations();
        this.currentState = new NormalState();
        this.respawnPosition = new Vector2(startPosition);
    }

    // --- GHOST TRAIL LOGIC ---
    public void spawnGhost() {
        if (currentFrame != null) {
            ghosts.add(new Ghost(position.x, position.y, currentFrame));
        }
    }

    private void updateGhosts(float delta) {
        Iterator<Ghost> iter = ghosts.iterator();
        while (iter.hasNext()) {
            Ghost g = iter.next();
            g.lifeTime -= delta;
            g.alpha = g.lifeTime / 0.5f;

            if (g.lifeTime <= 0) {
                iter.remove();
            }
        }
    }

    private void renderGhosts(SpriteBatch batch) {
        float oldR = batch.getColor().r;
        float oldG = batch.getColor().g;
        float oldB = batch.getColor().b;
        float oldA = batch.getColor().a;

        for (Ghost g : ghosts) {
            batch.setColor(0.5f, 1f, 1f, g.alpha * 0.5f);
            batch.draw(g.texture, g.x, g.y, width, height);
        }
        batch.setColor(oldR, oldG, oldB, oldA);
    }

    // --- STATE MANAGEMENT ---
    public void changeState(PlayerState newState) {
        this.currentState = newState;
        if (newState instanceof com.chronicorn.frontend.states.DashingState) {
            this.dashCooldownTimer = DASH_COOLDOWN;
            notifyDashObservers();
        }
    }

    public void handleInput() {
        currentState.handleInput(this);
    }

    public void attemptDash() {
        currentState.onDashCommand(this);
    }

    // --- MAIN UPDATE LOOP ---
    public void update(float delta) {
        if (dashCooldownTimer > 0) dashCooldownTimer -= delta;

        // Update Cooldown
        if (dashCooldownTimer > 0) {
            dashCooldownTimer -= delta;
            if (dashCooldownTimer < 0) dashCooldownTimer = 0;
            notifyDashObservers();
        }

        if (isInvincible) {
            invincibilityTimer -= delta;

            if (invincibilityTimer <= 0) {
                isInvincible = false;
            }
        }

        if (isHit) {
            hitTimer -= delta;
            if (hitTimer <= 0) isHit = false;
        }

        if (!dead) {
            currentState.update(this, delta);
            if (currentState instanceof NormalState) {
                currentState.handleInput(this);
            }
            position.mulAdd(velocity, delta);
            handleWalkingSound();
            updateGhosts(delta);
        }

        if (dead) {
            stopWalkingSound();
            return;
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
        observer.onHealthChanged(hp, maxHp);
        observer.onDashCooldownChanged(dashCooldownTimer, DASH_COOLDOWN);
        observer.onPlayerStatusChanged(this.dead);
    }

    public void removeObserver(PlayerObserver observer) {
        this.observers.remove(observer);
    }

    private void notifyStatusObservers() {
        for (PlayerObserver o : observers) {
            o.onPlayerStatusChanged(this.dead);
        }
    }

    private void notifyDashObservers() {
        for (PlayerObserver o : observers) {
            o.onDashCooldownChanged(dashCooldownTimer, DASH_COOLDOWN);
        }
    }

    private void notifyHealthObservers() {
        for (PlayerObserver o : observers) {
            o.onHealthChanged(hp, maxHp);
        }
    }

    public void limitSpeed() {
        velocity.clamp(0, maxSpeed);
    }

    public void setExactVelocity(float x, float y) {
        velocity.set(x, y);
    }

    public boolean isDashReady() {
        return dashCooldownTimer <= 0;
    }

    // --- GETTERS & SETTERS ---
    public Direction getCurrentDirection() { return currentDirection; }
    public void setCurrentDirection(Direction dir) { this.currentDirection = dir; }
    public boolean isDead() { return dead; }
    public boolean isMoving() { return velocity.len() > 10f; }
    public boolean isDashAchieved() {
        return GameSession.getInstance().isSet("SKILL_DASH");
    }

    // --- ANIMATION ---
    private void initializeAnimations() {
        textureSheet = new Texture(Gdx.files.internal("feschar_big_040.png"));
        TextureRegion[][] tmpFrames = TextureRegion.split(textureSheet, 48, 64);
        int[] rowMap = {1, 2, 0, 3};

        for (int i = 0; i < Direction.values().length; i++) {
            Direction dir = Direction.values()[i];
            int rowInImage = rowMap[i];
            TextureRegion[] frames = new TextureRegion[4];
            frames[0] = tmpFrames[rowInImage][0];
            frames[1] = tmpFrames[rowInImage][1];
            frames[2] = tmpFrames[rowInImage][2];
            frames[3] = tmpFrames[rowInImage][3];
            walkAnimations.put(dir, new Animation<>(1f/9f, frames));
            idleFrames.put(dir, tmpFrames[rowInImage][0]);
        }
    }

    private void updateAnimation(float delta) {
        if (!isMoving()) {
            stateTime = 0f;
        } else {
            stateTime += delta;
        }
        if (isMoving()) {
            currentFrame = walkAnimations.get(currentDirection).getKeyFrame(stateTime, true);
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
        renderGhosts(batch);

        if (currentFrame != null) {
            float drawX = position.x;
            float drawY = position.y;

            if (isHit) {
                batch.setColor(1f, 0f, 0f, 1f);

                float shakeIntensity = 5.0f;
                drawX += MathUtils.random(-shakeIntensity, shakeIntensity);
                drawY += MathUtils.random(-shakeIntensity, shakeIntensity);

            } else {
                batch.setColor(1f, 1f, 1f, 1f); // Warna Normal
            }
            batch.draw(currentFrame, drawX, drawY, width, height);

            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    public Vector2 getPosition() { return position; }
    public Vector2 getVelocity() { return velocity; }
    public Rectangle getBounds() { return collider; }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
        this.collider.setPosition(x, y);
    }

    private void updateCollider() {
        collider.setPosition(position.x, position.y);
    }

    public void dispose() {
        textureSheet.dispose();
    }

    public float getMaxSpeed() { return maxSpeed; }
    public boolean isDashing() { return currentState instanceof DashingState; }

    public int calculateBumpDamage(Monster enemy) {
        float baseDamage = (6 * BUMP_BASE_VALUE * ((float)this.atk / enemy.def) * 0.02f) + 2;
        if (isDashing()) {
            baseDamage *= 1.2f;
        }
        return (int) baseDamage;
    }

    public void setSpawnPoint(float x, float y) {
        this.position.set(x, y);
        this.collider.setPosition(x, y);
        if (this.respawnPosition == null) {
            this.respawnPosition = new Vector2(x, y);
        } else {
            this.respawnPosition.set(x, y);
        }
        this.velocity.set(0, 0);
    }

    public int getAtk() { return atk; }
    public int getDef() { return def; }
    public int getHp() { return hp; }

    public void takeDamage(int amount) {
        if (!dead) {
            this.hp -= amount;
            this.isHit = true;
            this.hitTimer = 0.2f; // Durasi getar & merah (0.2 detik)
            notifyHealthObservers();
            System.out.println("Player Hit! HP: " + hp);

            if (this.hp <= 0) {
                this.dead = true;
                stopWalkingSound();
                notifyStatusObservers();
            }

            SoundManager.getInstance().playSoundWithCooldown("horse_hit1.wav", 0.2f);
        }
    }

    public void takeHazardDamage(int amount) {
        if (isInvincible || isDead()) return;

        if (!dead) {
            this.hp -= amount;
            this.isHit = true;
            this.hitTimer = 0.2f; // Durasi getar & merah (0.2 detik)
            notifyHealthObservers();
            System.out.println("Player Hit! HP: " + hp);

            if (this.hp <= 0) {
                this.dead = true;
                stopWalkingSound();
                notifyStatusObservers();
            } else {
                isInvincible = true;
                invincibilityTimer = INVINCIBILITY_DURATION;
            }

            SoundManager.getInstance().playSoundWithCooldown("horse_hit1.wav", 0.2f);
        }
    }

    public void resetStats() {
        this.hp = maxHp;
        this.dead = false;
        this.isBusy = false;
        this.isHit = false;
        this.hitTimer = 0;
        this.velocity.set(0, 0);
        this.currentState = new NormalState();
        this.dashCooldownTimer = 0;
        this.ghosts.clear();
        stopWalkingSound();
        notifyStatusObservers();
        notifyHealthObservers();
        notifyDashObservers();
        if (respawnPosition != null) {
            setPosition(respawnPosition.x, respawnPosition.y);
        }
        System.out.println("Player stats reset. HP: " + hp);
    }

    public void gainHp(int number) {
        this.hp += number;

        if (this.hp > maxHp) {
            this.hp = maxHp;
        }

        notifyHealthObservers();

        System.out.println("HP Gained! Current HP: " + hp);
    }

    // --- SCORE ---
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int value) {
        this.score += value;
    }

    // --- NETWORK ---
    public void saveToCloud() {
        System.out.println("Mencoba menyimpan data ke server...");

        // Ambil username dari Main class
        String user = Main.currentUsername;

        com.chronicorn.frontend.managers.NetworkManager.getInstance().saveGame(
            user,
            this.score,
            this.hp,
            this.atk,
            this.def,
            new com.chronicorn.frontend.managers.NetworkCallback() {
                @Override
                public void onSuccess(String response) {
                    System.out.println("GAME SAVED: " + response);
                }

                @Override
                public void onError(String errorMessage) {
                    System.err.println("SAVE FAILED: " + errorMessage);
                }
            }
        );
    }
}
