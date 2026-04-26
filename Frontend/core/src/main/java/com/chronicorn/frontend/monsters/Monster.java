package com.chronicorn.frontend.monsters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.chronicorn.frontend.objects.PhysicsObjects;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.managers.SoundManager;

public abstract class Monster implements PhysicsObjects {
    public Vector2 position;
    public Rectangle bounds;

    // --- STATS ---
    public int hp;
    public int maxHp;
    public int atk;
    public int def;
    public boolean isHeavy;

    // --- STATUS ---
    public boolean isDead;
    public float pushResistance;

    // Cooldown agar tidak kena damage setiap frame (i-frame)
    public float damageCooldown = 0;
    private final float DAMAGE_INTERVAL = 0.3f;

    protected boolean isStunned = false;
    protected float stunTimer = 0;
    private final float STUN_DURATION = 0.3f;

    // --- VISUAL & ANIMASI ---
    protected Animation<TextureRegion> animation;
    protected float stateTime;
    protected float speed;

    protected boolean isHit = false;
    protected float hitTimer = 0;

    protected Vector2 velocity = new Vector2(0, 0);
    protected float friction = 2000f;

    // --- AI LOGIC ---
    public boolean isAggro = false;
    public float detectionRange;
    public float aggroTimer = 0;
    private final float aggroCooldown = 6f;

    // Wandering (Jalan-jalan santai)
    protected Vector2 wanderTarget;
    private float wanderPause = 0;
    private final float MAP_WIDTH = 800f;
    private final float MAP_HEIGHT = 600f;

    // --- AUDIO VARIABLES ---
    protected long walkSoundId = -1;
    protected String walkSoundFile = null; // Default null (anak class yg isi)
    protected float hearingDistance = 700f;

    public Monster(Animation<TextureRegion> animation, float x, float y, int hp, int atk, int def, boolean isHeavy, float speed, float pushResistance, float detectionRange) {
        this.animation = animation;
        this.position = new Vector2(x, y);
        this.hp = hp;
        this.maxHp = hp;
        this.atk = atk;
        this.def = def;
        this.isHeavy = isHeavy;
        this.speed = speed;
        this.pushResistance = pushResistance;
        this.detectionRange = detectionRange;
        this.isDead = false;
        this.stateTime = 0;

        if (animation != null) {
            TextureRegion firstFrame = animation.getKeyFrame(0);
            this.bounds = new Rectangle(x, y, firstFrame.getRegionWidth(), firstFrame.getRegionHeight());
        } else {
            this.bounds = new Rectangle(x, y, 32, 32);
        }

        this.wanderTarget = new Vector2(x, y);
    }

    // --- METHOD UPDATE UTAMA ---
    public void update(float delta, Player player) {
        stateTime += delta;

        if (damageCooldown > 0) damageCooldown -= delta;

        if (isHit) {
            hitTimer -= delta;
            if (hitTimer <= 0) isHit = false;
        }

        if (!isDead) {
            // Cek Aggro Logic (Deteksi Player)
            Vector2 playerPos = player.getPosition();
            float distanceToPlayer = position.dst(playerPos);

            if (distanceToPlayer <= detectionRange) {
                isAggro = true;
                aggroTimer = 0;
            } else {
                if (isAggro) {
                    aggroTimer += delta;
                    if (aggroTimer >= aggroCooldown) {
                        isAggro = false;
                        aggroTimer = 0;
                        wanderTarget.set(position);
                    }
                }
            }

            if (isStunned) {
                stunTimer -= delta;
                if (stunTimer <= 0) {
                    isStunned = false;
                }
                applyFriction(delta);
            } else {
                if (isAggro) {
                    updateAI(delta);
                } else {
                    updateWanderLogic(delta);
                }
            }

            position.mulAdd(velocity, delta);
            bounds.setPosition(position.x, position.y);
            handleWalkingSound(player);

        } else {
            stopWalkingSound();
        }
    }

    // --- PROXIMITY AUDIO LOGIC ---
    private void handleWalkingSound(Player player) {
        // Jika monster ini tidak punya suara jalan, skip
        if (walkSoundFile == null) return;

        boolean isMoving = velocity.len() > 10f;
        float distance = this.position.dst(player.getPosition());

        if (isMoving && distance < hearingDistance) {

            float rawVolume = 1.0f - (distance / hearingDistance);
            float finalVolume = rawVolume * SoundManager.getInstance().getSfxVolume();

            if (walkSoundId == -1) {
                walkSoundId = SoundManager.getInstance().loopSound(walkSoundFile);
            }
            SoundManager.getInstance().updateSoundVolume(walkSoundFile, walkSoundId, finalVolume);
        }
        else {
            // Jika diam Atau kejauhan, Matikan suara
            stopWalkingSound();
        }
    }

    protected void stopWalkingSound() {
        if (walkSoundId != -1) {
            SoundManager.getInstance().stopSound(walkSoundFile, walkSoundId);
            walkSoundId = -1;
        }
    }

    // --- LOGIKA WANDER (JALAN RANDOM) ---
    protected void updateWanderLogic(float delta) {
        if (wanderPause > 0) {
            wanderPause -= delta;
            velocity.set(0, 0);
            return;
        }

        if (position.dst(wanderTarget) < 10f) {
            wanderPause = 2f;
            float randomX = MathUtils.random(50, MAP_WIDTH - 50);
            float randomY = MathUtils.random(50, MAP_HEIGHT - 50);
            wanderTarget.set(randomX, randomY);
        } else {
            Vector2 dir = new Vector2(wanderTarget).sub(position).nor();
            velocity.set(dir.x * (speed * 0.5f), dir.y * (speed * 0.5f));
        }
    }

    public void takeDamage(int amount) {
        if (damageCooldown <= 0) {
            this.hp -= amount;
            damageCooldown = DAMAGE_INTERVAL;

            isHit = true;
            hitTimer = 0.2f;

            if (!isHeavy) {
                this.isStunned = true;
                this.stunTimer = STUN_DURATION;
                this.velocity.set(0, 0); // Stop sesaat
            }

            if (hp <= 0) {
                isDead = true;
                this.velocity.set(0, 0);
                stopWalkingSound(); // Matikan suara langkah
            }

            SoundManager.getInstance().playSoundWithCooldown("base_hit1.wav", 0.1f);
        }
    }

    public void applyFriction(float delta) {
        float currentSpeed = velocity.len();
        if (currentSpeed > 0) {
            float newSpeed = currentSpeed - (friction * delta);
            if (newSpeed < 0) newSpeed = 0;
            velocity.setLength(newSpeed);
        }
    }

    public void draw(SpriteBatch batch) {
        if (animation != null) {
            TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);

            float drawX = position.x;
            float drawY = position.y;

            if (isHit) {
                batch.setColor(Color.RED);

                // Efek Getar (Shake)
                float shakeIntensity = 5.0f;
                drawX += MathUtils.random(-shakeIntensity, shakeIntensity);
                drawY += MathUtils.random(-shakeIntensity, shakeIntensity);

            } else {
                batch.setColor(Color.WHITE);
            }

            batch.draw(currentFrame, drawX, drawY);
            batch.setColor(Color.WHITE);
        }
    }

    public Rectangle getBounds() { return bounds; }

    @Override
    public Vector2 getPosition() { return position; }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
        this.bounds.setPosition(x, y);
    }

    protected abstract void updateAI(float delta);
}
