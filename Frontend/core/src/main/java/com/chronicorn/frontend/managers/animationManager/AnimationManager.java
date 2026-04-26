package com.chronicorn.frontend.managers.animationManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.chronicorn.frontend.battlers.Battler;
import com.chronicorn.frontend.managers.battleManager.ui.EnemyWidget;
import com.chronicorn.frontend.skills.Action;

import java.util.HashMap;

public class AnimationManager {
    // --- STATIC CACHE (Global Memory) ---
    private static HashMap<String, Animation<TextureRegion>> vfxCache = new HashMap<>();

    // --- INSTANCE VARIABLES (UI Pointers) ---
    private Group enemyLayer;
    private Array<EnemyWidget> enemyWidgets;
    private JsonValue vfxDatabase;

    // Inject the UI dependencies when instantiating in BattleScreen
    public AnimationManager(Group enemyLayer, Array<EnemyWidget> enemyWidgets) {
        this.enemyLayer = enemyLayer;
        this.enemyWidgets = enemyWidgets;

        try {
            vfxDatabase = new JsonReader().parse(Gdx.files.internal("vfx/vfx_data.json"));
        } catch (Exception e) {
            Gdx.app.error("AnimationManager", "Could not load vfx_data.json", e);
            vfxDatabase = new JsonValue(JsonValue.ValueType.object); // Empty fallback
        }
    }

    // Static loader
    public static Animation<TextureRegion> getVFX(String vfxName, int columns, int rows, float frameDuration) {
        if (vfxCache.containsKey(vfxName)) {
            return vfxCache.get(vfxName);
        }

        Texture sheet = new Texture(Gdx.files.internal("vfx/" + vfxName + ".png"));
        sheet.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        int frameWidth = sheet.getWidth() / columns;
        int frameHeight = sheet.getHeight() / rows;

        TextureRegion[][] tmp = TextureRegion.split(sheet, frameWidth, frameHeight);
        TextureRegion[] frames = new TextureRegion[columns * rows];

        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                frames[index++] = tmp[i][j];
            }
        }

        Array<TextureRegion> framesArray = new Array<>(frames);
        Animation<TextureRegion> anim = new Animation<>(frameDuration, framesArray, Animation.PlayMode.NORMAL);
        vfxCache.put(vfxName, anim);

        return anim;
    }

    public static void dispose() {
        for (Animation<TextureRegion> anim : vfxCache.values()) {
            Object[] frames = anim.getKeyFrames();
            if (frames.length > 0) {
                ((TextureRegion) frames[0]).getTexture().dispose();
            }
        }
        vfxCache.clear();
    }

    // --- VISUAL EXECUTION METHODS ---

    public void playVFX(String vfxId, Action selectedAction, Runnable onAnimationDone) {
        // 1. Setup basic target data
        Battler primaryTarget = selectedAction.getPrimaryTarget();
        EnemyWidget targetWidget = findWidget(primaryTarget);

        if (targetWidget == null) {
            if (onAnimationDone != null) onAnimationDone.run();
            return;
        }

        float targetX = targetWidget.getX() + targetWidget.getWidth() / 2f;
        float targetY = targetWidget.getY() + targetWidget.getHeight() / 2f;

        // 2. Query the JSON Database using the string ID (e.g., "omnislash")
        JsonValue vfxData = vfxDatabase.get(vfxId);

        if (vfxData != null) {
            // -- JSON DATA FOUND: Execute Dynamic Choreography --

            // Get base image (defaults to the vfxId if "vfx" property isn't explicitly stated)
            String baseImage = vfxData.getString("vfx", vfxId);
            int columns = vfxData.getInt("column", 5);
            int rows = vfxData.getInt("row", 1);
            float frameDuration = vfxData.getFloat("duration", 0.05f);

            Animation<TextureRegion> vfx = getVFX(baseImage, columns, rows, frameDuration);

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
            sequence.addAction(Actions.run(onAnimationDone));

            // 2. Explicitly delete the VFXActor from the screen
            sequence.addAction(Actions.removeActor());

            vfxActor.addAction(sequence);
            enemyLayer.addActor(vfxActor);
        } else {
            // -- FALLBACK: No JSON entry exists. Play a standard 1:1 animation --
            Animation<TextureRegion> vfx = getVFX(vfxId, 5, 1, 0.05f);
            VFXActor vfxActor = new VFXActor(vfx, onAnimationDone);
            vfxActor.setPosition(targetX, targetY);
            enemyLayer.addActor(vfxActor);
        }
    }

    // Future expansion layout:
    // public void zoomCamera(float targetZoom, float duration) { ... }

    private EnemyWidget findWidget(Battler target) {
        for (EnemyWidget w : enemyWidgets) {
            if (w.getEnemy() == target) {
                return w;
            }
        }
        return null;
    }
}
