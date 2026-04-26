package com.chronicorn.frontend.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.chronicorn.frontend.Player;
import com.chronicorn.frontend.eventcommands.CmdFade;
import com.chronicorn.frontend.eventcommands.CmdTransferPlayer;
import com.chronicorn.frontend.eventcommands.CmdWindowFlex;
import com.chronicorn.frontend.managers.SoundManager; // Import SoundManager
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;

public class Gate extends InteractiveObject {

    private static Texture gateTexture;
    private static TextureRegion closedRegion;
    private static TextureRegion openRegion;

    private boolean isOpen = false;
    private float targetX;
    private float targetY;
    private String targetMapName;

    public static void loadAssets() {
        if (gateTexture == null) {
            gateTexture = new Texture(Gdx.files.internal("fes_Gate3.png"));
            TextureRegion[][] tmp = TextureRegion.split(gateTexture, 96, 96);
            closedRegion = tmp[0][0];
            openRegion = tmp[2][0];
        }
    }

    public static void disposeAssets() {
        if (gateTexture != null) {
            gateTexture.dispose();
            gateTexture = null;
        }
    }

    public Gate(String name, float x, float y) {
        super(name, x, y, 96, 96);

        if (gateTexture == null) {
            loadAssets();
        }

        updateVisuals();
    }

    private void updateVisuals() {
        if (isOpen) {
            currentFrame = openRegion;
            isSolid = false;
        } else {
            currentFrame = closedRegion;
            isSolid = true;
        }
    }

    public void open() {
        if (!isOpen) {
            isOpen = true;
            updateVisuals();

            // --- SUARA GATE TERBUKA DI SINI ---
            SoundManager.getInstance().playSound("gate.wav");
        }
    }

    @Override
    public void interact(Player player, EventManager events) {
        if (isOpen) {
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 5.0f));
            events.queue(new CmdTransferPlayer(targetMapName, targetX, targetY));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 5.0f));
            return;
        };

        events.queue(new CmdWindowFlex()
            .setText("Gate Locked! Find a Lever to Open!")
            .setBounds(
                Gdx.graphics.getWidth() / 2 - Gdx.graphics.getWidth() * 1/3,
                Gdx.graphics.getHeight() * 4/5,
                Gdx.graphics.getWidth() * 2/3,
                54
            )
            .setDuration(3f)
            .setBlocking(false)
        );
    }

    public Gate setTargetX(float x) {
        this.targetX = x;
        return this;
    }

    public Gate setTargetY(float y) {
        this.targetY = y;
        return this;
    }

    public Gate setMapName(String mapName) {
        this.targetMapName = mapName;
        return this;
    }
}
