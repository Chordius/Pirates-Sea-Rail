package com.chronicorn.frontend.managers.animationManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.chronicorn.frontend.battlers.Actor;
import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.battlers.Enemy;
import com.chronicorn.frontend.managers.battleManager.ui.ActorCardUI;
import com.chronicorn.frontend.managers.battleManager.ui.EnemyWidget;
import com.chronicorn.frontend.skills.Action;
import com.chronicorn.frontend.managers.battleManager.enums.TargetScope;

import java.util.HashMap;
import java.util.HashSet;

public class AnimationManager {
    // --- STATIC CACHE (Global Memory) ---
    private static final HashMap<String, Animation<TextureRegion>> vfxCache = new HashMap<>();
    private static JsonValue staticVfxDatabase;

    public static JsonValue getVfxDatabase() {
        if (staticVfxDatabase == null) {
            try {
                staticVfxDatabase = new JsonReader().parse(Gdx.files.internal("data/vfx_data.json"));
            } catch (Exception e) {
                Gdx.app.error("AnimationManager", "Could not load vfx_data.json", e);
                staticVfxDatabase = new JsonValue(JsonValue.ValueType.object);
            }
        }
        return staticVfxDatabase;
    }

    // --- INSTANCE VARIABLES (UI Pointers) ---
    private Group enemyLayer;
    private Group uiVfxLayer;
    private Array<EnemyWidget> enemyWidgets;
    private Array<ActorCardUI> actorCards;
    private JsonValue vfxDatabase;

    // Inject the UI dependencies when instantiating in BattleScreen
    public AnimationManager(Group enemyLayer, Group uiVfxLayer, Array<EnemyWidget> enemyWidgets,
            Array<ActorCardUI> actorCards) {
        this.enemyLayer = enemyLayer;
        this.uiVfxLayer = uiVfxLayer;
        this.enemyWidgets = enemyWidgets;
        this.actorCards = actorCards;

        this.vfxDatabase = getVfxDatabase();
    }

    // Static loader
    public static Animation<TextureRegion> getVFX(String vfxName, int columns, int rows, int startFrame, int endFrame,
            float frameDuration) {
        String cacheKey = vfxName
                + "_c" + columns
                + "_r" + rows
                + "_s" + startFrame
                + "_e" + endFrame
                + "_d" + frameDuration;

        if (vfxCache.containsKey(cacheKey)) {
            return vfxCache.get(cacheKey);
        }

        Texture sheet = new Texture(Gdx.files.internal("vfx/" + vfxName + ".png"));
        sheet.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        int frameWidth = sheet.getWidth() / columns;
        int frameHeight = sheet.getHeight() / rows;

        TextureRegion[][] tmp = TextureRegion.split(sheet, frameWidth, frameHeight);
        TextureRegion[] allFrames = new TextureRegion[columns * rows];

        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                allFrames[index++] = tmp[i][j];
            }
        }

        // Extract the specific requested frame range
        int frameCount = endFrame - startFrame + 1;
        TextureRegion[] selectedFrames = new TextureRegion[frameCount];
        System.arraycopy(allFrames, startFrame, selectedFrames, 0, frameCount);

        Array<TextureRegion> framesArray = new Array<>(selectedFrames);
        Animation<TextureRegion> anim = new Animation<>(frameDuration, framesArray, Animation.PlayMode.NORMAL);
        vfxCache.put(cacheKey, anim);

        return anim;
    }

    public static void dispose() {
        HashSet<com.badlogic.gdx.graphics.Texture> disposed = new HashSet<>();
        for (Animation<TextureRegion> anim : vfxCache.values()) {
            Object[] frames = anim.getKeyFrames();
            for (Object f : frames) {
                if (f instanceof TextureRegion) {
                    com.badlogic.gdx.graphics.Texture tex = ((TextureRegion) f).getTexture();
                    if (tex != null && !disposed.contains(tex)) {
                        try {
                            tex.dispose();
                        } catch (Exception ignored) {
                        }
                        disposed.add(tex);
                    }
                }
            }
        }
        vfxCache.clear();
    }

    // --- Find Helper ---
    public EnemyWidget findEnemyWidget(Battler battler) {
        for (EnemyWidget widget : enemyWidgets) {
            if (widget.getEnemy() == battler)
                return widget;
        }
        return null;
    }

    private ActorCardUI findActorWidget(Battler battler) {
        for (ActorCardUI card : actorCards) {
            if (card.getBattler() == battler)
                return card;
        }
        return null;
    }

    // --- VISUAL EXECUTION METHODS ---

    public static VFXActor createVFXActor(String vfxId, float targetX, float targetY, Runnable onAnimationDone) {
        JsonValue database = getVfxDatabase();
        JsonValue vfxData = database.get(vfxId);

        if (vfxData != null) {
            // Get base image (defaults to the vfxId if "vfx" property isn't explicitly
            // stated)
            String baseImage = vfxData.getString("vfx", vfxId);
            int columns = vfxData.getInt("column", 5);
            int rows = vfxData.getInt("row", 1);
            int startFrame = vfxData.getInt("startFrame", 0);
            int endFrame = vfxData.getInt("endFrame", (columns * rows) - 1);
            float frameDuration = vfxData.getFloat("duration", 0.05f);

            Animation<TextureRegion> vfx = getVFX(baseImage, columns, rows, startFrame, endFrame, frameDuration);

            VFXActor vfxActor = new VFXActor(vfx, null); // Callback goes to sequence
            vfxActor.setPosition(targetX, targetY);

            com.badlogic.gdx.scenes.scene2d.actions.SequenceAction sequence = Actions.sequence();

            JsonValue choreography = vfxData.get("choreography");
            if (choreography != null) {
                for (JsonValue cmd : choreography) {
                    com.badlogic.gdx.scenes.scene2d.Action parsedAction = DynamicActionParser.parse(cmd.asString());
                    if (parsedAction != null) {
                        sequence.addAction(parsedAction);
                    }
                }
            }

            // 1. Unlock the queue
            sequence.addAction(Actions.run(new Runnable() {
                @Override
                public void run() {
                    if (onAnimationDone != null) {
                        onAnimationDone.run();
                    }
                }
            }));

            // 2. Explicitly delete the VFXActor from the screen
            sequence.addAction(Actions.removeActor());

            vfxActor.addAction(sequence);
            return vfxActor;
        } else {
            // -- FALLBACK: No JSON entry exists. Play a standard 1:1 animation --
            Animation<TextureRegion> vfx = getVFX(vfxId, 5, 1, 0, 4, 0.05f);
            VFXActor vfxActor = new VFXActor(vfx, onAnimationDone != null ? onAnimationDone : () -> {
            });
            vfxActor.setPosition(targetX, targetY);
            return vfxActor;
        }
    }

    public void playVFX(String vfxId, Action selectedAction, Runnable onAnimationDone) {
        if (selectedAction == null || selectedAction.getPrimaryTarget() == null) {
            if (onAnimationDone != null)
                onAnimationDone.run();
            return;
        }

        // 1. Setup basic target data
        Battler primaryTarget = selectedAction.getPrimaryTarget();

        float targetX = 0;
        float targetY = 0;
        Group targetLayer = null;

        // Check if scope is ALL
        boolean isAllScope = (selectedAction.getSkill() != null
                && selectedAction.getSkill().getScope() == TargetScope.ALL);

        if (isAllScope) {
            if (enemyWidgets.size > 0) {
                float minX = Float.MAX_VALUE;
                float maxX = -Float.MAX_VALUE;
                float minY = Float.MAX_VALUE;
                float maxY = -Float.MAX_VALUE;
                for (int i = 0; i < enemyWidgets.size; i++) {
                    EnemyWidget widget = enemyWidgets.get(i);
                    float wX = widget.getX() + widget.getWidth() / 2f;
                    float wY = widget.getY() + widget.getHeight() / 2f;
                    if (wX < minX)
                        minX = wX;
                    if (wX > maxX)
                        maxX = wX;
                    if (wY < minY)
                        minY = wY;
                    if (wY > maxY)
                        maxY = wY;
                }
                targetX = (minX + maxX) / 2f;
                targetY = (minY + maxY) / 2f;
                targetLayer = enemyLayer;
            }
        }

        // Fallback: If not resolved via ALL scope, position based on primary target
        if (targetLayer == null) {
            if (primaryTarget instanceof Enemy) {
                EnemyWidget targetWidget = findEnemyWidget(primaryTarget);
                if (targetWidget == null) {
                    if (onAnimationDone != null)
                        onAnimationDone.run();
                    return;
                }
                targetX = targetWidget.getX() + targetWidget.getWidth() / 2f;
                targetY = targetWidget.getY() + targetWidget.getHeight() / 2f;
                targetLayer = enemyLayer;

            } else if (primaryTarget instanceof Actor) {
                ActorCardUI targetWidget = findActorWidget(primaryTarget);
                if (targetWidget == null) {
                    if (onAnimationDone != null)
                        onAnimationDone.run();
                    return;
                }

                // Convert the card's local center point into absolute stage coordinates
                Vector2 stageCoords = targetWidget.localToStageCoordinates(
                        new Vector2(targetWidget.getWidth() / 2f, targetWidget.getHeight() / 2f + 30));

                targetX = stageCoords.x;
                targetY = stageCoords.y;
                targetLayer = uiVfxLayer;
            }
        }

        if (targetLayer == null) {
            if (onAnimationDone != null)
                onAnimationDone.run();
            return;
        }

        VFXActor vfxActor = createVFXActor(vfxId, targetX, targetY, onAnimationDone);
        if (vfxActor != null) {
            targetLayer.addActor(vfxActor);
        }
    }

    // Future expansion layout:
    // public void zoomCamera(float targetZoom, float duration) { ... }

}
