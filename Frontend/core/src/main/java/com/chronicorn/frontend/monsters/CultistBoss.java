package com.chronicorn.frontend.monsters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Gdx;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.eventcommands.CmdWindowFlex;
import com.chronicorn.frontend.managers.SoundManager;
import com.chronicorn.frontend.managers.combatManager.MonsterManager;
import com.chronicorn.frontend.managers.combatManager.ProjectileManager;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.objects.Gate;
import com.chronicorn.frontend.objects.InteractiveObject;

import java.util.List;

public class CultistBoss extends Monster {
    public enum State { TELEPORT_OUT, TELEPORT_IN, ATTACK, SUMMON, COOLDOWN }

    private State currentState;
    private float stateTimer;
    private float alpha = 1f;

    private float summonCooldownTimer = 0;
    private boolean isActive = true;
    private boolean rageTriggered = false;
    private boolean deathLogicTriggered = false; // Flag agar logika mati cuma jalan sekali

    private long tpSoundId = -1;

    private List<Vector2> teleportPoints;
    private MonsterManager monsterManager;
    private static Texture healthBarTexture;
    private Player targetPlayer;

    public CultistBoss(float x, float y, List<Vector2> tpPoints, MonsterManager manager) {
        super(null, x, y, 1500, 20, 5, true, 0, 1.0f, 1000f);
        this.teleportPoints = tpPoints;
        this.monsterManager = manager;
        this.hearingDistance = 1200f;

        // --- Load Animation ---
        try {
            Texture tex = new Texture(Gdx.files.internal("Monsters/boss.png"));
            TextureRegion[][] tmp = TextureRegion.split(tex, tex.getWidth() / 3, tex.getHeight() / 4);
            TextureRegion[] frames = { tmp[0][0], tmp[0][1], tmp[0][2] };
            this.animation = new Animation<>(0.2f, frames);
            this.animation.setPlayMode(Animation.PlayMode.LOOP);
            this.bounds.setSize(tmp[0][0].getRegionWidth(), tmp[0][0].getRegionHeight());
        } catch (Exception e) {
            this.bounds.setSize(32, 32);
        }

        // --- Healthbar setup ---
        if (healthBarTexture == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            healthBarTexture = new Texture(pixmap);
            pixmap.dispose();
        }

        this.currentState = State.SUMMON;
    }

    @Override
    public void update(float delta, Player player) {
        this.targetPlayer = player;
        super.update(delta, player);
    }

    @Override
    public void takeDamage(int amount) {
        if (!isActive || monsterManager.getMinionCount() > 0) return;

        super.takeDamage(amount);
        if (this.hp <= 0 && !deathLogicTriggered) {
            deathLogicTriggered = true; // Kunci agar tidak double trigger

            // 1. Set Flag
            GameSession.getInstance().set("LB_BOSS_DEFEATED");
            System.out.println("BOSS DEFEATED: Flag LB_BOSS_DEFEATED set to true.");

            // 2. Mainkan suara
            SoundManager.getInstance().playSound("gate.wav");

            // 3. Buka Visual Gate
            InteractiveObject gateObj = LevelMapManager.getInstance().getObjectByName("Gate");
            if (gateObj != null && gateObj instanceof Gate) {
                ((Gate) gateObj).open();
                System.out.println("Gate opened by Boss Death.");
            } else {
                System.out.println("GATE NOT FOUND IN MAP MANAGER!");
            }
        }
    }

    @Override
    protected void updateAI(float delta) {
        if (!isActive) return;
        stateTimer += delta;

        if (summonCooldownTimer > 0) summonCooldownTimer -= delta;

        // --- LOGIKA RAGE MODE TRIGGER ---
        if (hp < maxHp * 0.3f && !rageTriggered && currentState != State.SUMMON) {
            currentState = State.SUMMON;
            stateTimer = 0;
            System.out.println("BOSS ENRAGED! PREPARING FINAL WAVE!");
            SoundManager.getInstance().playAmbient("ambients3.wav");
        }

        switch(currentState) {
            case ATTACK:
                float attackDelay = rageTriggered ? 0.3f : 0.5f;
                if (stateTimer > attackDelay) {
                    fire8Directions();
                    currentState = State.COOLDOWN;
                    stateTimer = 0;
                }
                break;
            case SUMMON:
                float castDuration = rageTriggered ? 2.5f : 1.0f;
                if (stateTimer > castDuration) {
                    summonMinions();
                    currentState = State.COOLDOWN;
                    stateTimer = 0;
                }
                break;
            case COOLDOWN:
                float cooldownTime = rageTriggered ? 1.5f : 4.0f;
                if (stateTimer > cooldownTime) {
                    decideNextMove();
                    stateTimer = 0;
                }
                break;
            case TELEPORT_OUT:
                float fadeSpeedOut = rageTriggered ? 3.0f : 2.0f;
                alpha -= delta * fadeSpeedOut;
                if (alpha <= 0) {
                    alpha = 0;
                    smartTeleport();
                    if (tpSoundId != -1) {
                        SoundManager.getInstance().stopSound("horse_power1.wav", tpSoundId);
                        tpSoundId = -1;
                    }
                    SoundManager.getInstance().playSound("rewind1.wav", 0.2f);
                    currentState = State.TELEPORT_IN;
                }
                break;
            case TELEPORT_IN:
                float fadeSpeedIn = rageTriggered ? 3.0f : 2.0f;
                alpha += delta * fadeSpeedIn;
                if (alpha >= 1) {
                    alpha = 1;
                    currentState = State.ATTACK;
                    stateTimer = 0;
                }
                break;
        }
    }

    private void decideNextMove() {
        int currentMinions = monsterManager.getMinionCount();
        boolean canSummon = (currentMinions < 4) && (summonCooldownTimer <= 0);
        if (canSummon && MathUtils.randomBoolean(0.5f)) {
            currentState = State.SUMMON;
        } else {
            currentState = State.TELEPORT_OUT;
            tpSoundId = SoundManager.getInstance().playSound("horse_power1.wav", 0.2f);
        }
    }

    private void fire8Directions() {
        float cx = position.x + bounds.width / 2;
        float cy = position.y + bounds.height / 2;
        SoundManager.getInstance().playSound("behold_thunder1.wav", 0.05f);
        for (int i = 0; i < 8; i++) {
            ProjectileManager.getInstance().spawnBullet(cx, cy, i * 45);
        }
    }

    private void summonMinions() {
        int amountToSpawn = 2;
        if (hp < maxHp * 0.3f && !rageTriggered) {
            amountToSpawn = 4;
            rageTriggered = true;
            System.out.println("BOSS SKILL: RAGE MODE (4 MINIONS)!");
        } else {
            int currentCount = monsterManager.getMinionCount();
            if (currentCount > 2) return;
            System.out.println("BOSS SKILL: NORMAL (2 MINIONS)");
        }
        SoundManager.getInstance().playSound("mage_pillar1.wav", 0.1f);
        EventManager events = LevelMapManager.getInstance().getEventManager();
        monsterManager.spawnBossMinions(amountToSpawn);
        if (rageTriggered) summonCooldownTimer = 18.0f;
        else summonCooldownTimer = 10.0f;

        events.queue(new CmdWindowFlex()
            .setText("Defeat the minions to hurt the boss!")
            .setBounds(
                Gdx.graphics.getWidth() / 2 - Gdx.graphics.getWidth() * 1/3,
                Gdx.graphics.getHeight() * 3/4,
                Gdx.graphics.getWidth() * 2/3,
                54
            )
            .setDuration(3f)
            .setBlocking(false)
        );
    }

    private void smartTeleport() {
        if (teleportPoints == null || teleportPoints.isEmpty()) return;
        Vector2 targetPos = null;
        if (MathUtils.random() < 0.3f) {
            int idx = MathUtils.random(0, teleportPoints.size() - 1);
            targetPos = teleportPoints.get(idx);
        } else {
            float maxDist = -1;
            Vector2 playerPos = targetPlayer.getPosition();
            for (Vector2 p : teleportPoints) {
                float dist = p.dst(playerPos);
                if (dist > maxDist) {
                    maxDist = dist;
                    targetPos = p;
                }
            }
        }
        if (targetPos != null) position.set(targetPos);
    }

    @Override
    public void draw(SpriteBatch batch) {
        if(animation == null) return;
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
        Color originalColor = batch.getColor();
        float drawX = position.x;
        float drawY = position.y;

        if (isInvincible()) {
            batch.setColor(1f, 1f, 1f, 0.5f);
            drawX += MathUtils.random(-1f, 1f);
            drawY += MathUtils.random(-1f, 1f);
        } else if (currentState == State.SUMMON) {
            batch.setColor(1f, 0f, 1f, alpha);
            float shake = 3.0f;
            drawX += MathUtils.random(-shake, shake);
            drawY += MathUtils.random(-shake, shake);
        } else if (isHit) {
            batch.setColor(1f, 0f, 0f, alpha);
            float shake = 5.0f;
            drawX += MathUtils.random(-shake, shake);
            drawY += MathUtils.random(-shake, shake);
        } else {
            batch.setColor(1f, 1f, 1f, alpha);
        }

        batch.draw(currentFrame, drawX, drawY);

        batch.setColor(originalColor);
        if (!isDead && alpha > 0.5f) {
            float barWidth = 60f;
            float barHeight = 8f;
            float barX = position.x + (bounds.width - barWidth) / 2;
            float barY = position.y + bounds.height + 10;
            float hpPercent = (float) hp / maxHp;
            if (hpPercent < 0) hpPercent = 0;

            batch.setColor(Color.RED);
            batch.draw(healthBarTexture, barX, barY, barWidth, barHeight);
            batch.setColor(Color.GREEN);
            batch.draw(healthBarTexture, barX, barY, barWidth * hpPercent, barHeight);
        }
        batch.setColor(originalColor);
    }

    public boolean isInvincible() {
        return monsterManager.getMinionCount() > 0;
    }

    public void setActive(boolean active) {
        this.isActive = active;
        if (active) {
            this.stateTimer = 0;
            SoundManager.getInstance().playSound("boss_roar.wav", 0.5f);
        }
    }

    public boolean isActive() { return isActive; }
}
